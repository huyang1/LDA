package huyang.edu.cn.NLP;

import java.util.List;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLPSegment {

    public static void main(String[] args) {

        // 载入自定义的Properties文件
        StanfordCoreNLP pipeline = new StanfordCoreNLP("CoreNLP-chinese.properties");

        // 用一些文本来初始化一个注释。文本是构造函数的参数。
        Annotation annotation;

        annotation = pipeline.process("我爱北京天安门");

        // 从注释中获取CoreMap List，并取第0个值
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentence = sentences.get(0);

        // 从CoreMap中取出CoreLabel List，逐一打印出来
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        System.out.println("字/词");
        System.out.println("-----------------------------");
        for (CoreLabel token : tokens) {
            String word = token.getString(TextAnnotation.class);
//            String pos = token.getString(PartOfSpeechAnnotation.class);
//            String ner = token.getString(NamedEntityTagAnnotation.class);
            System.out.println(word);
        }

    }

}
