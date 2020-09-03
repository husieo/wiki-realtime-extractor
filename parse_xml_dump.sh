if [ ! -f target/extractor-0.0.1-SNAPSHOT.jar ]; then
    echo "Executable not found, building the project"
    mvn clean install
fi
java -jar target/extractor-0.0.1-SNAPSHOT.jar "$@"