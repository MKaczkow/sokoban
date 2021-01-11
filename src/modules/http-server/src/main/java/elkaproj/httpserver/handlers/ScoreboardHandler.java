package elkaproj.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import elkaproj.DebugWriter;
import elkaproj.config.IScoreboard;
import elkaproj.httpserver.ServiceProvider;
import elkaproj.httpserver.services.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Handler("/scoreboard")
public class ScoreboardHandler implements HttpHandler {

    private static final String ROUTE_TEMPLATE = "/scoreboard/:pack/:level?";

    private final ServiceProvider serviceProvider;
    private final IService<EndpointParserService> endpointParserService;
    private final IService<ErrorHandlerService> errorHandlerService;
    private final IService<PostgresHandler> postgresHandlerService;

    private ScoreboardHandler(
            ServiceProvider serviceProvider,
            @Inject(EndpointParserService.class) IService<EndpointParserService> endpointParserService,
            @Inject(ErrorHandlerService.class) IService<ErrorHandlerService> errorHandlerService,
            @Inject(PostgresHandler.class) IService<PostgresHandler> postgresHandlerService) {
        this.serviceProvider = serviceProvider;
        this.endpointParserService = endpointParserService;
        this.errorHandlerService = errorHandlerService;
        this.postgresHandlerService = postgresHandlerService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        EndpointParserService eps = this.endpointParserService.getInstance(this.serviceProvider);
        Map<String, String> args = eps.parse(httpExchange.getRequestURI().getPath(), ROUTE_TEMPLATE);
        if (args == null || !args.containsKey("pack")) {
            this.errorHandlerService.getInstance(this.serviceProvider).write404(httpExchange);
            return;
        }

        Map<String, String> query = URLEncodedUtils.parse(httpExchange.getRequestURI(), StandardCharsets.UTF_8).stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        String player = query.getOrDefault("player", null);

        switch (httpExchange.getRequestMethod()) {
            case "POST":
                if (this.addNew(httpExchange, args, player))
                    return;
                break;

            case "GET":
                if (this.get(httpExchange, args, player))
                    return;
                break;
        }

        this.errorHandlerService.getInstance(this.serviceProvider).write400(httpExchange);
    }

    private boolean addNew(HttpExchange httpExchange, Map<String, String> args, String player) throws IOException {
        if (!args.containsKey("level") || player == null || player.equals(""))
            return false;

        int score;
        try (InputStream is = httpExchange.getRequestBody()) {
            try (Scanner s = new Scanner(is)) {
                score = s.nextInt();
            }
        } catch (Exception ex) {
            this.errorHandlerService.getInstance(this.serviceProvider).write400(httpExchange);
            return true;
        }

        try {
            PostgresHandler postgres = this.postgresHandlerService.getInstance(this.serviceProvider);
            postgres.open();

            postgres.writeEntry(args.get("pack"), Integer.parseInt(args.get("level")), player, score);

            postgres.close();

            httpExchange.sendResponseHeaders(204, -1);
            httpExchange.close();

            DebugWriter.INSTANCE.logMessage("GDAT-HSW", "Created scoreboard: %s/%d %s %d", args.get("pack"), Integer.parseInt(args.get("level")), player, score);
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("DB-SCORE", ex, "Couldn't write score.");
            this.errorHandlerService.getInstance(this.serviceProvider).write500(httpExchange);
        }

        return true;
    }

    private boolean get(HttpExchange httpExchange, Map<String, String> args, String player) throws IOException {
        String pack = args.get("pack");
        int level = -1;
        try {
            level = Integer.parseInt(args.get("level"));
        } catch (Exception ignored) {
        }

        try {
            PostgresHandler postgres = this.postgresHandlerService.getInstance(this.serviceProvider);
            postgres.open();

            IScoreboard scoreboard;
            if (level >= 0) {
                if (player != null && !player.equals(""))
                    scoreboard = PostgresHandler.createScoreboard(pack, postgres.getEntryFor(player.toUpperCase(), pack, level));
                else
                    scoreboard = PostgresHandler.createScoreboard(pack, postgres.getEntriesForLevel(pack, level));
            } else {
                if (player != null && !player.equals(""))
                    scoreboard = PostgresHandler.createScoreboard(pack, postgres.getPlayerEntriesForPack(pack, player.toUpperCase()));
                else
                    scoreboard = PostgresHandler.createScoreboard(pack, postgres.getEntriesForPack(pack));
            }

            postgres.close();

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                scoreboard.serialize(baos);

                try (OutputStream os = httpExchange.getResponseBody()) {
                    httpExchange.sendResponseHeaders(200, baos.size());
                    os.write(baos.toByteArray());
                }
            }

            DebugWriter.INSTANCE.logMessage("GDAT-HSR", "Retrieved scoreboard: %s/%d %s", pack, level, player);
        } catch (Exception ex) {
            DebugWriter.INSTANCE.logError("DB-SCORE", ex, "Couldn't read score.");
            this.errorHandlerService.getInstance(this.serviceProvider).write500(httpExchange);
        }

        return true;
    }
}
