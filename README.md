# Syntactically Annotated Wikipedia Dump Tool

The Syntactically Annotated Wikipedia Dump (SAWD) tool is a Java implementation used to create a syntactic parse from a set of folders containing the Wikipedia raw text. This text can be obtained by using G. Attardi WikiExtractor Python script. The parse is for English currently (we are in process of parsing other languages). It uses the CoreNLP  suite of tools.

The input to this program is the folder where the output of [WikiExtractor](https://github.com/attardi/wikiextractor) is located. Ususally it is a number of folders named `AA`, `AB`, `AC`, etc. with clean Wikipedia text inside (clean means no tables, no images, no markup, etc.).

## Dependencies:

They are all included in the Maven pom.xml file, so you just have to make your programming framework download them (or use mvn of course).

Nonetheless, the Stanford Shift-Reduce parser is needed. You can [download](http://nlp.stanford.edu/software/srparser.shtml) it from the official website. Choose option 2 of the available packages (we use _stanford-srparser-2014-10-23-models.jar_). 

Finally, just put this file into the `target/dependecy/` folder. Note that each time the project is built with Maven, it will copy all the dependencies (if not already there) to that same folder. This is for convenience, to run the program directly with the java command. The downside is that you will have in two different paths the Stanford models file (around 300MB) as well as the srparser file.

We use Java 8 as the latest version of Stanford's CoreNLP.

## Using the tool:

It is easier to build with Maven. So, install Maven in your computer and run 
`mvn package` . This will build the package and copy the required dependencies.

To run the program, from the root folder, do: 

`java -cp target/SAWD-maker-1.0-SNAPSHOT.jar:target/dependency/* edu.eric.text2stanford.WikiParser  -i /media/stuff/Pavel/Documents/Eclipse/workspace/data/these_graph/wikidata/AA -t 4`

`-i` indicates the path of the cleaned text Wikipedia. 

`-t` indicates the number of threads to use.


