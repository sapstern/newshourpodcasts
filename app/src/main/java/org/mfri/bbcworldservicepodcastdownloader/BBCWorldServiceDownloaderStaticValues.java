package org.mfri.bbcworldservicepodcastdownloader;

import java.util.HashMap;

public interface BBCWorldServiceDownloaderStaticValues {

  long MILLIS_PER_12H = 12 * 60 * 60 * 1000L;

  String PROGRAM_NEWSHOUR    = "newshour";
  String PROGRAM_SPORTSWORLD = "sportsworld";
  String PROGRAM_SPORTSHOUR  = "sportshour";
  String PROGRAM_BUSINESSDAILY  = "businessdaily";
  String PROGRAM_GLOBALNEWS  = "globalnews";


  HashMap<String, String>  URL_MAP =  new HashMap<String, String>(){{put("globalnews", "https://www.bbc.co.uk/programmes/p02nq0gn/episodes/downloads");put("businessdaily", "https://www.bbc.co.uk/programmes/p002vsxs/episodes/downloads");put("newshour", "https://www.bbc.co.uk/programmes/p002vsnk/episodes/downloads");put("sportsworld", "https://www.bbc.co.uk/programmes/p002w5vq/episodes/downloads");put("sportshour", "https://www.bbc.co.uk/programmes/p016tmfz/episodes/downloads");}};



}
