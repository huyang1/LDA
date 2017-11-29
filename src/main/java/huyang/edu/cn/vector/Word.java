package huyang.edu.cn.vector;

import java.io.*;
import java.util.ArrayList;

public class Word implements Vector<String> {

    public static final String SPLIT = " ";
    private ArrayList<String> words;
    private int size;

    public Word() {
        this.words = new ArrayList<String>();
        this.size = 0;
    }

    public Word(int size, ArrayList<String> data ) {
        this.words = data;
        this.size = size;
    }

    public void readFromFile(String path) {
        File file = new File(path);
        BufferedReader br = null;
        String str = null;
        try {
            br = new BufferedReader(new FileReader(file));
            while ((str=br.readLine())!=null) {
                String[] word = str.split(SPLIT);
                for(String data : word) {
                    this.words.add(data);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.size = this.words.size();
    }

    public void readFromArray() {

    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getValue(int index) {
        return this.words.get(index);
    }

    public void setValue(int index, String value) {
        this.words.set(index, value);
    }

    public int getSize() {
        return this.words.size();
    }

    public void add(String value) {

    }


    public Vector like() {
        return (Vector) this.words.clone();
    }

}
