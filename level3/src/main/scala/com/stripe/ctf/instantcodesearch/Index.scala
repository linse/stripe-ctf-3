package com.stripe.ctf.instantcodesearch

import java.io._
import com.twitter.concurrent.Broker;
import scala.collection.mutable.HashMap;

class Index(repoPath: String) extends Serializable {
  var files = List[String]()
  def path() = repoPath
  var sa = new SuffixArray(repoPath)
  var fileToNr = new HashMap[String,Int]
  var fileNr = 0;

  def addFile(file: String, text: String) = {
    files = file :: files
    fileToNr.put(file,fileNr)
    fileNr = fileNr + 1
    // foreach line in file
//    sa.addFile(file)
    //sa.show()
  }

  def fileToNumber(file: String):Int = {
     System.out.println("File "+file+" to nr")
     fileToNr.get(file) match {
       case Some(value) => value
       case None => 0
     }
  }  

  def sort() = {
    sa.sort()
  }

  def findMe(needle: String,b: Broker[SearchResult]) = {
    sa.findMe(needle,b);
  }

}
