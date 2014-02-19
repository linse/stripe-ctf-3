package com.stripe.ctf.instantcodesearch;

/*************************************************************************
 *  Compilation:  javac SuffixArray.java
 *  Execution:  java SuffixArray input.txt
 *
 *  A data type that computes the suffix array of a string.
 *
 *   $ java SuffixArray abra.txt
 *    i ind lcp rnk  select
 *   ---------------------------
 *    0  11   -   0  "!"
 *    1  10   0   1  "A!"
 *    2   7   1   2  "ABRA!"
 *    3   0   4   3  "ABRACADABRA!"
 *    4   3   1   4  "ACADABRA!"
 *    5   5   1   5  "ADABRA!"
 *    6   8   0   6  "BRA!"
 *    7   1   3   7  "BRACADABRA!"
 *    8   4   0   8  "CADABRA!"
 *    9   6   0   9  "DABRA!"
 *   10   9   0  10  "RA!"
 *   11   2   2  11  "RACADABRA!"
 *
 *************************************************************************/

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.twitter.concurrent.Broker;
import com.twitter.util.Await;



public class SuffixArray {
    String text = "";
    private ArrayList<Suffix> suffixes;
    private ArrayList<String> filenames;
    String path;
    int N;

    // walk also over files
    // the word / suffix does not exist for us if it is not in the dictionary of queries!
    // this way the index can be smaller
    public SuffixArray(String path) {
       this.path = path;
       this.N = 0;
       this.suffixes = new ArrayList<Suffix>();
       this.text = "";
       this.filenames = new ArrayList<String>();
    }

    public SuffixArray(String path, String[] filelist) {
       this(path);
       addFiles(filelist);
    }

    public void addFiles(String[] filelist) {
       for (int i=0;i<filelist.length;i++) {
          this.addFile(filelist[i]);
       }
    }

    public void sort() {
       Collections.sort(this.suffixes);
    }

    public void addFile(String file) {
        try {
          // add file to filenames
          this.filenames.add(new String(file));
          // increase file number
          String newtext = new Scanner(new File(this.path+"/"+file)).useDelimiter("\\Z").next();
          int line = 1;
          int offset = this.N; // Index in the entire text body
          this.N += newtext.length();
          this.text += newtext;
          // notify for efficient growth
          this.suffixes.ensureCapacity(this.N);
          for (int i = offset; i < this.text.length(); i++) {
              if (this.text.charAt(i)=='\n') {
                line++;
                continue;
              }
              if (! Character.isLetter(this.text.charAt(i))) {
                continue;
              }
              // we could skip suffixes beginning with space, but that is more complicated later
              //System.out.println("new suffix from file "+filenames.size());
              suffixes.add(new Suffix(i, line, filenames.size()));
          }
        } 
        catch (FileNotFoundException e) {
          e.printStackTrace();
        }
    }

    public class Suffix implements Comparable<Suffix> {
        private final int index; // it has the offset, too
        private final int line;
        private final int fromFileNo;

        private Suffix(int index, int line, int fromFileNo) {
            this.index = index;
            this.line = line;
            this.fromFileNo = fromFileNo;
        }
        private int length() {
            return text().length() - index;
        }
        private char charAt(int i) {
            return text().charAt(index + i);
        }

        public int compareTo(Suffix that) {
            if (this == that) return 0;  // optimization
            int N = Math.min(this.length(), that.length());
            for (int i = 0; i < N; i++) {
                if (this.charAt(i) < that.charAt(i)) return -1;
                if (this.charAt(i) > that.charAt(i)) return +1;
            }
            return this.length() - that.length();
        }

        public String toString() {
            return text().substring(index);
        }

        public String text() {
           return text;
        }

    }

    public int length() {
        return suffixes.size();
    }

    public int index(int i) {
        if (i < 0 || i >= suffixes.size()) throw new IndexOutOfBoundsException();
        return suffixes.get(i).index;
    }

    public int line(int i) {
        if (i < 0 || i >= suffixes.size()) throw new IndexOutOfBoundsException();
        return suffixes.get(i).line;
    }

    public String file(int i) {
        if (i < 0 || i >= suffixes.size()) throw new IndexOutOfBoundsException();
        return filenames.get(fromFileNo(i)-1);
    }

    public int fromFileNo(int i) {
        if (i < 0 || i >= suffixes.size()) throw new IndexOutOfBoundsException();
        return suffixes.get(i).fromFileNo;
    }

    // length of lcp of i-th smallest suffix and i-1st smallest suffix
    public int lcp(int i) {
        if (i < 1 || i >= suffixes.size()) throw new IndexOutOfBoundsException();
        return lcp(suffixes.get(i), suffixes.get(i-1));
    }

    // longest common prefix of s and t
    private static int lcp(Suffix s, Suffix t) {
        int N = Math.min(s.length(), t.length());
        for (int i = 0; i < N; i++) {
            if (s.charAt(i) != t.charAt(i)) return i;
        }
        return N;
    }

    // ith smallest suffix as a string.
    public String select(int i) {
        if (i < 0 || i >= suffixes.size()) throw new IndexOutOfBoundsException();
         return suffixes.get(i).toString();
    }

    // number of suffixes strictly less than the query string.
    // Note: rank(select(i)) equals i for each i
    // between 0 and N-1.
    public int rank(String query) {
        int lo = 0, hi = suffixes.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = compare(query, suffixes.get(mid));
            if (cmp < 0) hi = mid - 1;
            else if (cmp > 0) lo = mid + 1;
            else return mid;
        }
        return lo;
    }

    // compare query string to suffix
    private static int compare(String query, Suffix suffix) {
        int N = Math.min(query.length(), suffix.length());
        for (int i = 0; i < N; i++) {
            if (query.charAt(i) < suffix.charAt(i)) return -1;
            if (query.charAt(i) > suffix.charAt(i)) return +1;
        }
        return query.length() - suffix.length();
    }

    public void show() {
        String s = this.text;
        System.out.println("Text s"+s);
        System.out.println("  i ind lin lcp rnk select");
        System.out.println("---------------------------");

        for (int i = 0; i < this.suffixes.size(); i++) {
            int index = this.index(i);
            System.out.println("Index "+index+" for i "+i); 
            int line = this.line(i);
            int fromFileNo = this.fromFileNo(i);
            String ith = "\"" + s.substring(index, Math.min(index + 50, s.length())) + "\"";
            //String ith = "NONO";
            assert s.substring(index).equals(this.select(i));
            int rank = this.rank(s.substring(index));
            if (i == 0) {
                System.out.printf("%3d %3d %3d %3d %3s %3d %s\n", i, index, fromFileNo, line, "-", rank, ith);
            }
            else {
                int lcp = this.lcp(i);
                System.out.printf("%3d %3d %3d %3d %3d %3d %s\n", i, index, fromFileNo, line, lcp, rank, ith);
            }
        }
    }

    public static String join(String arr[], String sep) { 
      String joined = new String();
      for (int i=0; i < arr.length-1;i++) {
         joined += (arr[i]+sep);
      }
      if (arr.length>1)
         joined += arr[arr.length-1];
      return joined;
    }

    public void findPrint(String query) {
          for (int i = this.rank(query); i < N; i++) {
              int from1 = this.index(i);
              int to1   = Math.min(N, from1 + query.length());
              if (!query.equals(text.substring(from1, to1))) {
                 //System.out.println("We found nothing, but look at the Array:");
                 //this.show();
                 break;
              }
              System.out.print(text.substring(from1, to1));
              int line  = this.line(i);
              String file = this.file(i);
              System.out.println(" in line "+line+" and file "+file);
          }
          System.out.println();
    }

    public void findMe(String query, Broker<SearchResult> b) {
          try {
             for (int i = this.rank(query); i < N; i++) {
                 int from1 = this.index(i);
                 int to1   = Math.min(N, from1 + query.length());
                 if (!query.equals(text.substring(from1, to1))) {
                    break;
                 }
                 int line  = this.line(i);
                 String file = this.file(i);
                 int fromFileNo = this.fromFileNo(i);
                 SearchResult msg = new Match(fromFileNo,file,line);
                 Await.result(b.send(msg).sync());
             }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
    }

//    // java SuffixArray longstring query context
//    public static void main(String[] args) {
//
//          String[] filenames = {"/home/linse/stripe/suffixarray/text","/home/linse/stripe/suffixarray/text1"};
//          String query = args[1];
//
//          // build suffix array for a list of files 
//          SuffixArray sa = new SuffixArray(filenames);
//          sa.sort();
//
//          //sa.show();
//          sa.find(query);
//
//    } 
} 



