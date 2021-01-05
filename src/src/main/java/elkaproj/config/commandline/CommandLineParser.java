package elkaproj.config.commandline;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses command line arguments and produces objects of specified type T.
 * @param <T> Type of objects this parser produces.
 */
public class CommandLineParser<T> {

    private final Argument[] args;
    private final Class<T> klass;

    /**
     * Creates a new command line parser for specified type.
     * @param klass Type of objects this parser will produce.
     */
    public CommandLineParser(Class<T> klass) {
        this.klass = klass;
        this.args = this.buildOptionList(this.klass);
    }

    /**
     * Parses supplied command line arguments, and returns a command line options object of type T.
     * @param args Arguments to parse.
     * @return Populated command line options object.
     * @throws IllegalArgumentException Parsing failed due to invalid argument, invalid value, or other forms of invalid input.
     */
    public T parse(String[] args) throws IllegalArgumentException {
        Map<String, Argument> fullNames = Arrays.stream(this.args)
                .collect(Collectors.toMap(x -> x.fullName, x -> x));

        Map<Character, Argument> shorthands = Arrays.stream(this.args)
                .filter(x -> x.shorthand != '\0')
                .collect(Collectors.toMap(x -> x.shorthand, x -> x));

        HashSet<Argument> processedArguments = new HashSet<>();

        T val;
        try {
            Constructor<T> ctor = klass.getDeclaredConstructor();
            ctor.setAccessible(true);
            val = ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid options class.", e);
        }

        Argument state = null;
        for (String arg : args) {
            if (state != null) {
                this.setValueFor(arg, state, val, String.valueOf(state.shorthand));
                processedArguments.add(state);
                state = null;

                continue;
            }

            if (arg.charAt(0) != '-')
                throw new IllegalArgumentException("Invalid option supplied: '" + arg + "'");

            if (arg.charAt(1) == '-') {
                // parse long name
                this.parseLongArg(arg.substring(2), val, fullNames, processedArguments);
            } else {
                // parse potentially series of shorthands
                state = this.parseShortArgs(arg.substring(1), val, shorthands, processedArguments);
            }
        }

        if (state != null)
            throw new IllegalArgumentException("Missing value for " + state.shorthand);

        for (Argument arg : this.args) {
            if (processedArguments.contains(arg)) {
                continue;
            }

            try {
                if (arg.type == CommandLineArgumentType.FLAG)
                    arg.field.set(val, false);
                else
                    arg.field.set(val, arg.defaultValue);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Cannot set default value for " + arg.fullName);
            }
        }

        return val;
    }

    private void parseLongArg(String argv, T target, Map<String, Argument> nameMap, HashSet<Argument> processedArguments) {
        int nameEnd = argv.indexOf('=');

        boolean hasValue = false;
        String name = argv;
        if (nameEnd == 0) {
            throw new IllegalArgumentException("Empty string is not a valid argument name.");
        } else if (nameEnd > 0) { // no value specified
            name = name.substring(0, nameEnd);
            hasValue = true;
        }

        if (!nameMap.containsKey(name)) {
            throw new IllegalArgumentException(name + " is not a valid argument name.");
        }

        Argument arg = nameMap.get(name);
        if ((arg.type == CommandLineArgumentType.FLAG) == hasValue) { // is flag but has value
            throw new IllegalArgumentException(name + " is a flag argument and takes no value.");
        }

        if (!hasValue) {
            try {
                arg.field.set(target, true);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Couldn't set value for " + name, ex);
            }

            processedArguments.add(arg);
            return;
        }

        this.setValueFor(argv.substring(nameEnd + 1), arg, target, name);
        processedArguments.add(arg);
    }

    // returns the state to hold
    private Argument parseShortArgs(String argv, T target, Map<Character, Argument> nameMap, HashSet<Argument> processedArguments) {
        if (argv.length() < 1)
            throw new IllegalArgumentException("Empty string is not a valid argument name.");

        for (int i = 0; i < argv.length(); i++) {
            char c = argv.charAt(i);
            if (!nameMap.containsKey(c))
                throw new IllegalArgumentException(c + " is not a valid argument name.");

            Argument arg = nameMap.get(c);
            if (arg.type == CommandLineArgumentType.FLAG) {
                try {
                    arg.field.set(target, true);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Couldn't set value for " + c, ex);
                }

                processedArguments.add(arg);
                continue;
            }

            if (argv.length() == (i + 1)) {
                return arg;
            }

            this.setValueFor(argv.substring(i + 1), arg, target, String.valueOf(c));
            processedArguments.add(arg);
            break;
        }

        return null;
    }

    private void setValueFor(Object value, Argument arg, T target, String name) {
        try {
            if (arg.type == CommandLineArgumentType.NUMBER) {
                value = Integer.valueOf((String)value);
            }

            arg.field.set(target, value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Couldn't set value for " + name, ex);
        }
    }

    /**
     * Prints out help for the specified type, to the specified output.
     * @param output Output to write help to. Typically {@link System#out}.
     */
    public void printHelp(PrintStream output) {
        output.println("Available options:");
        output.println();
        for (Argument arg : this.args) {
            output.print("  --");
            output.print(arg.fullName);

            if (arg.type != CommandLineArgumentType.FLAG) {
                output.print("=value");
            }

            if (arg.shorthand != '\0') {
                output.print(" | -");
                output.print(arg.shorthand);

                if (arg.type != CommandLineArgumentType.FLAG) {
                    output.print("value | -");
                    output.print(arg.shorthand);

                    if (arg.type != CommandLineArgumentType.FLAG) {
                        output.print(" value");
                    }
                }
            }

            output.println();
            output.print("    ");
            output.println(arg.helpText);
            output.println();
        }
    }

    private Argument[] buildOptionList(Class<?> klass) {
        Field[] fields = klass.getDeclaredFields();
        ArrayList<Argument> argumentModels = new ArrayList<>();
        for (Field f : fields) {
            CommandLineArgument arg = f.getAnnotation(CommandLineArgument.class);
            if (arg == null)
                continue;

            Argument argModel = new Argument();
            argModel.fullName = arg.name();
            argModel.shorthand = arg.shorthand();
            argModel.type = arg.type();
            argModel.helpText = arg.helpText();
            argModel.field = f;
            f.setAccessible(true);

            String dval = arg.defaultValue();

            if (f.getType() == Boolean.class || f.getType() == Boolean.TYPE) {
                argModel.defaultValue = false;
            } else if (f.getType() == String.class) {
                if (dval.equals(""))
                    dval = null;

                argModel.defaultValue = dval;
            } else if (f.getType() == Integer.class || f.getType() == Integer.TYPE) {
                try {
                    argModel.defaultValue = Integer.valueOf(dval);
                } catch (Exception ignored) {
                    throw new IllegalArgumentException("Argument for " + f.getName() + " (" + arg.name() + ") has invalid default value of '" + dval + "'.");
                }
            } else {
                throw new IllegalArgumentException("Argument for " + f.getName() + " (" + arg.name() + ") has invalid type of '" + f.getType().getName() + "'.");
            }

            argumentModels.add(argModel);
        }

        Argument[] argumentModelArray = new Argument[argumentModels.size()];
        argumentModels.toArray(argumentModelArray);
        return argumentModelArray;
    }

    private static class Argument {
        public String fullName;
        public char shorthand;
        public CommandLineArgumentType type;
        public Object defaultValue;
        public String helpText;
        public Field field;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Argument argument = (Argument) o;
            return fullName.equals(argument.fullName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fullName);
        }
    }
}
