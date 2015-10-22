//Created by E.P. Soriano Morales

package edu.eric.text2stanford;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;



public class ParserThread implements Runnable {


    public final String nline = "\n";
    public final String header = "token\tlemma\tPOS\tconstituency\thead\tdependency";
    public String pathFile;
    public StanfordCoreNLP coreParser;
    //    Constructor
    ParserThread(String pathFile, StanfordCoreNLP coreParser) {
        this.coreParser = coreParser;
        this.pathFile = pathFile;
    }


    public HashMap<String, String> getAnchors(Elements anchors) {
        HashMap<String, String> anchorMap = new HashMap<>();

        for (Element a : anchors) {

            String href = a.attr("href");
            String hrefWord = a.text();
//            System.out.println("\t" + hrefWord + ": " + href);
            anchorMap.put(href, hrefWord);

        }

        return anchorMap;
    }


    public HashMap<Integer, HashMap> coreNLPTokenDependencies(SemanticGraph depGraph) {
        /**
         * This function takes a SemanticGraph object (with the typed dependencies of a sentence) and returns
         * a dictionary of dictionaries: {wordIndex:{ "relation": subj", "headIndex": "2"}, ...}
         * Each word of the phrase has a dict with each dependency it belongs to and its corresponding head.
         */
        HashMap<Integer, HashMap> tokenDeps = new HashMap<>();
        Collection<TypedDependency> typedDeps = depGraph.typedDependencies();
        for (TypedDependency depn : typedDeps) {
            HashMap<String, String> relationHead = new HashMap<>();
            int wordIndex = depn.dep().index();
            int headIndex = depn.gov().index();
            String relation = depn.reln().getShortName();

//            if (depn.reln().getSpecific() != null)
//                relation += "_" + depn.reln().getSpecific();

            relationHead.put("relation", relation);
            relationHead.put("headIndex", Integer.toString(headIndex));
            tokenDeps.put(wordIndex, relationHead);
        }

        return tokenDeps;
    }

    public HashMap<Integer, ArrayList> coreNLPTokenConstituents(Tree tree) {
        HashMap<Integer, ArrayList> tokenTags = new HashMap<>();
        List<Tree> children = tree.getChildrenAsList();
        ArrayList<String> listTags = new ArrayList<>();
        //Traverse depth search first the tree looking for all the constituents

//        String whereareweDEBUG = tree.label().value();
        for (Tree son : children)
            if (!son.isLeaf()) {
                tokenTags.putAll(coreNLPTokenConstituents(son));
            } else {
                tokenTags.put(((CoreLabel) son.label()).index(), listTags);
            }
        if (!tree.isPreTerminal()) // I do not want the POS tag in this list
            for (Integer key : tokenTags.keySet())
                tokenTags.get(key).add(tree.label().value() + "_" + Integer.toString(tree.hashCode() % 100));

        return tokenTags;
    }




    public void run() {
        System.out.print("WORKING on " + pathFile + "\n");
        parseWiki(pathFile);
        System.out.println("... DONE");

    }

    private void parseWiki(String pathFile)  {
        try {

            File input = new File(pathFile);
            String parsedFilePath = input.getCanonicalPath() + ".parsed";
            FileWriter parsedOutput = new FileWriter(parsedFilePath);
            BufferedWriter bufferedOut = new BufferedWriter(parsedOutput);

            Document doc;
            doc = Jsoup.parse(input, "UTF-8", "");

            bufferedOut.write("FILENAME " + input.getName() + nline);
            bufferedOut.write(header + nline);

            // Foreach doc in the file
            Elements docs = doc.getElementsByTag("doc");
            for (Element c : docs) {
                String articleTile = c.attr("title");
                String docText = c.text();
                docText = docText.replaceFirst(Pattern.quote(articleTile), ""); ///> Remove the title from the content
//                System.out.println(docText);
                bufferedOut.write("%%#PAGE " + articleTile + nline);


                // Get anchors (links to other wiki articles) used in the document
                HashMap<String, String> anchorMap = getAnchors(c.getElementsByTag("a"));

                // Parse the document with CoreNLP
//                docText = "A great brigand becomes a ruler of a Nation";

                Annotation document = new Annotation(docText);

                coreParser.annotate(document);
                // Treat the result
                List<CoreMap> sentences = document.get(SentencesAnnotation.class);
                int sentenceId = 0;
                for (CoreMap sentence : sentences) {
                    List<CoreLabel> listTokens = sentence.get(TokensAnnotation.class);


                    int sentenceSize = listTokens.size();
                    String line;
                    sentenceId++;
                    bufferedOut.write("%%#SEN\t" + Integer.toString(sentenceId) + "\t" + Integer.toString(sentenceSize) + nline);

                    // this is the parse tree of the current sentence
                    Tree tree = sentence.get(TreeAnnotation.class);
//                    String pennString = tree.pennString();
//                    System.out.println(sentence.toString());
                    HashMap<Integer, ArrayList> constituencyTokens = coreNLPTokenConstituents(tree.skipRoot());
                    Map<Integer, HashMap> dependencyTokens = null;
                    //> Here we determine which dependency parser to use according to the language. We also find the lemmas.
                    // if it is english we use only the Stanford Parser.

                    // this is the dependency graph of the current sentence
                    SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
                    dependencyTokens = coreNLPTokenDependencies(dependencies);
                    // traversing the words in the current sentence
                    // a CoreLabel is a CoreMap with additional token-specific methods
                    String head;
                    String dependency;
                    String lemma;

                    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                        // this is the text of the token
                        String word = token.get(TextAnnotation.class);
                        // this is the index of said token
                        int wordIndex = token.get(IndexAnnotation.class);
                        // this is the POS tag of the token
                        String pos = token.get(PartOfSpeechAnnotation.class);
                        // this is the constituency information of the token
                        String constituency = String.join(",", constituencyTokens.get(wordIndex));


                        if (WikiParser.wikiLanguage.equals("en"))

                            lemma = token.get(LemmaAnnotation.class);
                        else
                            lemma = (String) dependencyTokens.get(wordIndex).get("lemma");

                        if (dependencyTokens.get(wordIndex) == null) {
                            head = "0";
                            dependency = "PUNCT";
                        } else {
                            head = (String) dependencyTokens.get(wordIndex).get("headIndex");
                            // the relation (dependency label)
                            dependency = (String) dependencyTokens.get(wordIndex).get("relation");
                        }


                        // create the line that will be written in the output
                        line = word + "\t" + lemma + "\t" + pos + "\t" + constituency + "\t" + head + "\t" + dependency + nline;
                        bufferedOut.write(line);

                    }//foreach token

                }//foreach sentence

            }//foreach page
            bufferedOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

