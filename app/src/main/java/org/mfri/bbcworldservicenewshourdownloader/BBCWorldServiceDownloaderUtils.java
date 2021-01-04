package org.mfri.bbcworldservicenewshourdownloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.net.ConnectivityManagerCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class BBCWorldServiceDownloaderUtils implements BBCWorldServiceDownloaderStaticValues {


    Bundle currentDownloadOptions = null;
    Date timeStampOfcurrentDownloadOptions = null;
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
        Log.d("Utils", "getDownloadedPodcasts() start" );
        Bundle bundle = new Bundle();
        ArrayList<TempDLItem> tab = new ArrayList<>();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/"+BBCWorldServiceDownloaderStaticValues.BBC_PODCAST_DIR);
        File[] podcastArry = myDir.listFiles();
        if(podcastArry==null)
            return null;


        int counter = 0;
        //Date lastDate = new Date(-3600000);
        for (int i=0; i<podcastArry.length; i++) {
            if(     podcastArry[i]!=null
                 &&!podcastArry[i].isDirectory()
                 && podcastArry[i].getName().startsWith("Newshour_")
                 && podcastArry[i].getName().endsWith(".mp3")
              ){
                counter++;
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
                        if(!currentToken.startsWith("Newshour"))
                            theDescriptionBuilder.append(currentToken);
                        if(toki.hasMoreElements())
                            theDescriptionBuilder.append(" ");
                    }
                }

                String dateString = theDateBuilder.toString();
                TempDLItem localItem = new TempDLItem("X", theDescriptionBuilder.toString(), "none", dateString, theFileName);
                localItem.compareDate = getDateFromPatternString(dateString);
                tab.add(localItem);
            }
        }
        Collections.sort(tab);
        for(int i=0;i<tab.size();i++)
        {
            TempDLItem currentItem = tab.get(i);
            DownloadListItem item = new DownloadListItem(String.valueOf(i),currentItem.content,currentItem.url,currentItem.dateOfPublication,currentItem.fileName);
            bundle.putParcelable("ITEM_" +i, item);
        }
        Log.d("Utils", "getDownloadedPodcasts() end size of list: "+counter );
        bundle.putInt("LIST_SIZE", counter);
        return bundle;
    }

    public Date getDateFromPatternString(String patternString){
        DateFormat df = new SimpleDateFormat("EEE_dd_MMMMMMMMMMM_yyyy_kk_mm", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return df.parse(patternString);
        } catch (ParseException e) {
            e.printStackTrace();
           return null;
        }
    }
    /*
     * Checks connection state for wlan
     * used in ListService as well as BackgroundDownloadService
     * @return
     * */
    public synchronized Bundle getCurrentDownloadOptions() throws IOException {

        if(currentDownloadOptions!=null && isWithinTimeFrame(timeStampOfcurrentDownloadOptions)) {
            return currentDownloadOptions;
        }
        currentDownloadOptions = new Bundle();
        timeStampOfcurrentDownloadOptions = new Date();

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
                currentDownloadOptions.putParcelable("ITEM_" + s, item);
                s++;
            }
            Log.d("onHandleIntent size: ", String.valueOf(s));
            currentDownloadOptions.putInt("LIST_SIZE", s);
        }

        timeStampOfcurrentDownloadOptions = new Date();
        Log.d("HANDLE", "handleActionDownloadList exit");
        return currentDownloadOptions;
    }
    /**
     * checks whether we are now less than 12 hours after the last run
     * If not, returns false
     *
     * @param timeStampOfcurrentDownloadOptions
     * @return
     */
    private boolean isWithinTimeFrame(Date timeStampOfcurrentDownloadOptions) {

        if(timeStampOfcurrentDownloadOptions==null)
            return false;
        Date currentDate = new Date();
        //first check simply whether last timstamp is more than 12 hours
        if (Math.abs(timeStampOfcurrentDownloadOptions.getTime() - currentDate.getTime()) > MILLIS_PER_12H)
            return false;

        //Get a GMT timezone calendar
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        cal.setTime(currentDate);
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        //Check whether last relevant publication date was at 15 00 GMT or 22 00 GMT
        boolean isBetween15And22 = currentHour > 15 && currentHour < 22;
        if(isBetween15And22){
            cal.set(Calendar.HOUR, 15);
        }
        else{
            cal.set(Calendar.HOUR, 22);
        }
        cal.set(Calendar.MINUTE, 00);
        //Now compare the timestamp of the last run was made after the last publication date
        //Then we do not need to fetch the currently available downloads again
        if(timeStampOfcurrentDownloadOptions.getTime() > cal.getTime().getTime())
            return true;

        return false;

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
     * @param isStartedInBackground whether this service will be started from background task
     * @return
     */
    public Intent prepareItemDownload(DownloadListItem item, Context theContext, boolean isToastOnFileExists, boolean isStartedInBackground) {
        Bundle bundle = new Bundle();
        bundle.putString("url", item.url);
       //String fileName = prepareFilename(item.content,item.dateOfPublication);

        bundle.putString("fileName", item.fileName);
        bundle.putBoolean("isToastOnFileExists", isToastOnFileExists);
        bundle.putBoolean("isStartedInBackground", isStartedInBackground);
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
        File myDir = new File(root + "/"+BBCWorldServiceDownloaderStaticValues.BBC_PODCAST_DIR);
        if (!myDir.exists()) {
            return null;
        }
        File theFile = new File(root + "/"+BBCWorldServiceDownloaderStaticValues.BBC_PODCAST_DIR+"/" + fileName);
        if (theFile.exists()) {
            return theFile;
        }
        return null;
    }
    public void showNotification(String title, String message, boolean isIntend, String fileNameWithoutDir, Context context, NotificationManager mNotificationManager) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1234567",
                    "BBC_POD_DL",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Download podcasts ongoing");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1234567")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
//        if (isIntend) {
//            Intent intent = new Intent(getApplicationContext(), MediaPlayerActivity.class);
//            //MFRI hier noch mediaplayer activity richtig aufrufen
//            intent.putExtra("fileNameWithoutDir", fileNameWithoutDir);
//            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(pi);
//        }
        mNotificationManager.notify(0, mBuilder.build());
    }
}
