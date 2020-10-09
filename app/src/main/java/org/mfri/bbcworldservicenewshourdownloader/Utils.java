package org.mfri.bbcworldservicenewshourdownloader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.core.net.ConnectivityManagerCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class Utils {

    /*
     * Checks the network state for connection
     *
     * */
    public static boolean isDeviceConnected(Context theContext) {
        ConnectivityManager cm = (ConnectivityManager) theContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /*
     * Checks connection state for wlan
     *
     * */
    public static boolean isDeviceOnWlan(Context theContext) {
        ConnectivityManager cm = (ConnectivityManager) theContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return !ConnectivityManagerCompat.isActiveNetworkMetered(cm);
    }


    public synchronized Bundle getDownloadedPodcasts() throws IOException{

        Bundle bundle = new Bundle();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Podcasts");
        File[] podcastArry = myDir.listFiles();
        if(podcastArry==null)
            return null;
        int i=0;
        for (; i<podcastArry.length;i++) {
            if(     podcastArry[i]!=null
                 &&!podcastArry[i].isDirectory()
                 && podcastArry[i].getName().startsWith("Newshour_")
                 && podcastArry[i].getName().endsWith(".mp3")
              ){
                String theFileName = podcastArry[i].getName();
                StringTokenizer toki = new StringTokenizer(podcastArry[i].getName().substring(0, podcastArry[i].getName().indexOf(".mp3")), "_");
                StringBuilder theDescriptionBuilder = new StringBuilder();
                StringBuilder theDateBuilder = new StringBuilder();
                boolean isDate = false;
                while (toki.hasMoreElements()){
                    String currentToken = toki.nextToken();
                    if( currentToken.equals("Mon")
                        ||  currentToken.equals("Tue")
                        ||  currentToken.equals("Wed")
                        ||  currentToken.equals("Thu")
                        ||  currentToken.equals("Fri")
                        ||  currentToken.equals("Sat")
                        ||  currentToken.equals("Sun")
                    ){
                      isDate = true;
                    }
                    if(isDate){
                        theDateBuilder.append(currentToken);
                        if(toki.hasMoreElements())
                             theDateBuilder.append("_");
                    }else {
                        theDescriptionBuilder.append(currentToken);
                        if(toki.hasMoreElements())
                            theDescriptionBuilder.append(" ");
                    }
                }
                DownloadListItem item = new DownloadListItem(String.valueOf(i), theDescriptionBuilder.toString(), "none", theDateBuilder.toString(), theFileName);
                bundle.putParcelable("ITEM_" + i, item);
            }
        }
        bundle.putInt("SIZE", i);
        return bundle;
    }
    /*
     * Checks connection state for wlan
     * used in ListService as well as BackgroundDownloadService
     * @return
     * */
    public synchronized Bundle getCurrentDownloadOptions() throws IOException {
        Bundle bundle = new Bundle();
        //MFRI jsoup rein
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";
        Document doc = null;
        doc = Jsoup.connect("https://www.bbc.co.uk/programmes/p002vsnk/episodes/downloads").userAgent(userAgent).get();
        Elements theElements = doc.select("a[href]");
        String publicationDate = "Mon 01 Januar 0000, 00:00";
        int i = 0;
        int s = 0;
        for (; i < theElements.size(); i++) {

            if (theElements.get(i).attr("title") != null && theElements.get(i).attr("title").indexOf("days left to listen") != -1) {
                String theTitle = theElements.get(i).attr("title");
                Log.d("TITLE", theTitle);
                int startIndex = theTitle.indexOf("(");
                int endIndex = theTitle.indexOf(")");
                if (startIndex != -1 && endIndex != -1) {

                    startIndex = startIndex + 1;
                    publicationDate = theTitle.substring(startIndex, endIndex);
                    //publicationDate = (publicationDate + "_" + theTitle.substring(startIndex, endIndex)).trim();
                    //Thu 24 September 2020, 15:00
                    publicationDate = getPublicationDate(publicationDate);
                    Log.d("PUB_DATE", publicationDate);
                }
            }
            if (theElements.get(i).text().startsWith("Lower quality (64kbps)")) {
                Log.d("ELEMENT", theElements.get(i).text());
                Log.d("ATTRIBUT_TEXT", theElements.get(i).attr("download"));
                Log.d("ATTRIBUT_HREF", theElements.get(i).attr("href"));
                String theDescription = theElements.get(i).attr("download").substring(0, theElements.get(i).attr("download").indexOf("-"));
                theDescription = theDescription.replace("Newshour,", "").trim();
                String theFilename = prepareFilename(theDescription, publicationDate );
                
                DownloadListItem item = new DownloadListItem(String.valueOf(s), theDescription, "https:" + theElements.get(i).attr("href"), publicationDate, theFilename);
                publicationDate = "Mon 01 Januar 0000, 00:00";
                bundle.putParcelable("ITEM_" + s, item);
                s++;
            }
            Log.d("onHandleIntent size: ", String.valueOf(s));
            bundle.putInt("LIST_SIZE", s);
        }

        Log.d("HANDLE", "handleActionDownloadList exit");
        return bundle;
    }

    /**
     * converts date string from bbc html to publication date in subtracting 29 days
     * e.G. Wed 23 September 2020, 22:00 will result in Tuesday, 25 August 2020
     *
     * @param dateInString
     * @return
     */
    private static String getPublicationDate(String dateInString) {

        DateFormat format = new SimpleDateFormat("E d MMMM yyyy, HH:mm", Locale.ENGLISH);
        try {

            Date date = format.parse(dateInString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, -30);
            date = cal.getTime();
            return format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Mon 01 Januar 0000, 00:00";
        }

    }

    /**
     * Creates an Intent for DownloadService for Downloading a single podcast
     *
     * @param item                current item
     * @param theContext
     * @param isToastOnFileExists If we need notification and toast on the info that there is a file of the given name already sitting in filesystem
     * @return
     */
    public Intent prepareItemDownload(DownloadListItem item, Context theContext, boolean isToastOnFileExists) {
        Bundle bundle = new Bundle();
        bundle.putString("url", item.url);
       //String fileName = prepareFilename(item.content,item.dateOfPublication);

        bundle.putString("fileName", item.fileName);
        bundle.putBoolean("isToastOnFileExists", isToastOnFileExists);
        Intent intent = new Intent(theContext, DownloadService.class);
        intent.putExtras(bundle);
        return intent;

    }

    private String prepareFilename(String theContentDesc, String theDate) {
        theDate = theDate.replace(",", "");
        theDate = theDate.replaceAll(" ", "_");
        theDate = theDate.replaceAll(":", "_");
        theContentDesc = theContentDesc.replaceAll(" ", "_");
        theContentDesc = theContentDesc.replaceAll(":", "_");
        for (int i = 0; i < 10; i++) {
            if (theContentDesc.indexOf("'") == -1)
                break;
            theContentDesc = theContentDesc.replace("'", "_");
        }
        return "Newshour_"+theContentDesc+"_"+theDate+".mp3";
    }



    /**
     * Check for existance of given filename in podcasts directory
     *
     * @param fileName
     * @return
     */
    public File fileExists(String fileName) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Podcasts");
        if (!myDir.exists()) {
            return null;
        }
        File theFile = new File(root + "/Podcasts/" + fileName);
        if (theFile.exists()) {
            return theFile;
        }
        return null;
    }

}
