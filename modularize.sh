WORKDIR=/tmp/modules

mkdir -p "$WORKDIR"
cd "$WORKDIR"
mkdir "$WORKDIR/jars"

function modularize() {
    original_jar="$1"
    module_name="$2"

    cp "$original_jar" "$WORKDIR/jars"
    echo "Generating module-info.java..."
    jdeps --generate-module-info . "$original_jar"

    echo "Compiling module-info.java..."
    jar_name=$(basename "$original_jar")
    javac --module-path jars --patch-module "$module_name=jars/$jar_name" "$module_name/module-info.java"

    echo "Adding module-info.class to $jar_name..."
    jar vuf "jars/$jar_name" -C "$module_name" module-info.class

    echo "Replacing $original_jar with the new one..."
    cp "$WORKDIR/jars/$jar_name" "$original_jar"
}

#modularize "$HOME/.m2/repository/commons-cli/commons-cli/1.5.0/commons-cli-1.5.0.jar" "commons.cli"
#modularize "$HOME/.m2/repository/javazoom/jlayer/1.0.1/jlayer-1.0.1.jar" "jlayer"
#modularize "$HOME/.m2/repository/net/benjaminguzman/Pipe/1.0.2/Pipe-1.0.2.jar" "Pipe"

rm -rf "$WORKDIR"