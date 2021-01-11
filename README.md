# Projekt PROZE, Semestr 20Z
## Sokoban
Autorzy:
- Brawański Mateusz
- Kaczkowski Maciej

## Dokumentacja
- Dokumentacja dostępna jest [tutaj](https://static.emzi0767.com/misc/eiti/proze/).

## Uruchamianie projektu
Żeby skompilować i uruchomić projekt, należy użyć Gradle'a.

Plik JAR zostanie utworzony w katalogu `src/build/libs`.

### Windows
```batch
cd src
gradlew.bat shadowJar
```

### Linux, OS X
```sh
cd src
sh gradlew shadowJar
```

### Uruchomienie z katalogu projektu
Żeby uruchomić będąc w katalogu projektu (`src`): `java -jar build/libs/Sokoban-1.0-SNAPSHOT-all.jar`

Można przy użyciu wiersza poleceń zmieniać niektóre opcje programu, takie jak język. Żeby uzyskać więcej informacji, 
wykonaj program z wiersza poleceń z argumentem `--help`.

Do uruchomienia wymagany jest folder `config` dostępny w katalogu projektu (`src`).
