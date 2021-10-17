package org.mfri.bbcworldservicenewshourdownloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class BBCWorldServiceDownloaderUtils implements BBCWorldServiceDownloaderStaticValues {

    private Bundle currentDownloadOptions = null;
    private Date timeStampOfcurrentDownloadOptions = null;

    private PeriodicWorkRequest downLoadRequest = null;

    private static Constraints DOWNLOADCONSTRAINTS = null;

    private static final BBCWorldServiceDownloaderUtils INSTANCE = new BBCWorldServiceDownloaderUtils();


    private BBCWorldServiceDownloaderUtils(){
    }

    public static BBCWorldServiceDownloaderUtils getInstance()
    {
        return INSTANCE;
    }

    public PeriodicWorkRequest getDownLoadRequest(){
        if(downLoadRequest == null){
            downLoadRequest = new PeriodicWorkRequest.Builder(DownloadWorker.class, 1, TimeUnit.HOURS)
                    .setConstraints(getDownLoadConstraints())
                    .build();
        }
        return downLoadRequest;
    }

    private static Constraints getDownLoadConstraints(){
        if (DOWNLOADCONSTRAINTS==null) {
            DOWNLOADCONSTRAINTS =  new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build();
        }
        return DOWNLOADCONSTRAINTS;
    }

    /*
     * Checks the network state for connection
     *
     * */
    public static boolean isDeviceConnected(Context theContext) {
        ConnectivityManager cm = (ConnectivityManager) theContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public synchronized Bundle getDownloadedPodcastsBundle(Context context) throws IOException{
        Log.d("Utils", "getDownloadedPodcasts() start" );
        Bundle bundle = new Bundle();
        ArrayList<TempDLItem> tab = getDownloadedPodcastsList(context);
        if (tab == null) return null;
        //Convert TempDLItem to DownloadListItem
        for(int i=0;i<tab.size();i++)
        {
            TempDLItem currentItem = tab.get(i);
            DownloadListItem item = new DownloadListItem(String.valueOf(i),currentItem.content,currentItem.url,currentItem.dateOfPublication,currentItem.fileName);
            bundle.putParcelable("ITEM_" +i, item);
        }
        Log.d("Utils", "getDownloadedPodcasts() end size of list: "+tab.size() );
        bundle.putInt("LIST_SIZE", tab.size());
        return bundle;
    }

    @Nullable
    public ArrayList<TempDLItem> getDownloadedPodcastsList(Context context) {
        ArrayList<TempDLItem> tab = new ArrayList<>();
        String root = PreferenceManager.getDefaultSharedPreferences(context).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
        File myDir = new File(root );
        File[] podcastArry = myDir.listFiles();
        if(podcastArry==null)
            return null;


        //int counter = 0;

        for (File file : podcastArry) {
            if (file != null
                    && !file.isDirectory()
                    && file.getName().startsWith("Newshour_")
                    && file.getName().endsWith(".mp3")
            ) {
                //counter++;
                String theFileName = file.getName();
                StringTokenizer toki = new StringTokenizer(file.getName().substring(0, file.getName().indexOf(".mp3")), "_");
                StringBuilder theDescriptionBuilder = new StringBuilder();
                StringBuilder theDateBuilder = new StringBuilder();
                boolean isDate = false;
                while (toki.hasMoreElements()) {
                    String currentToken = toki.nextToken();
                    if (currentToken.equals("Mon")
                            || currentToken.equals("Tue")
                            || currentToken.equals("Wed")
                            || currentToken.equals("Thu")
                            || currentToken.equals("Fri")
                            || currentToken.equals("Sat")
                            || currentToken.equals("Sun")
                    ) {
                        isDate = true;
                    }
                    if (isDate) {
                        theDateBuilder.append(currentToken);
                        if (toki.hasMoreElements())
                            theDateBuilder.append("_");
                    } else {
                        if (!currentToken.startsWith("Newshour"))
                            theDescriptionBuilder.append(currentToken);
                        if (toki.hasMoreElements())
                            theDescriptionBuilder.append(" ");
                    }
                }

                String dateString = theDateBuilder.toString();
                TempDLItem localItem = new TempDLItem("X", theDescriptionBuilder.toString(), "none", dateString, theFileName);
                localItem.compareDate = getDateFromPatternString(dateString);
                tab.add(localItem);
            }
        }
        //Sort by date descending
        Collections.sort(tab);
        return tab;
    }

    /**
     * @param patternString
     * @return
     */
    public Date getDateFromPatternString(String patternString){
        DateFormat df = new SimpleDateFormat("EEE_dd_MMMMMMMMMMM_yyyy_kk_mm", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return getParsedDate(patternString, df);
    }

    private Date getParsedDate(String patternString, DateFormat df) {
        try {
            return df.parse(patternString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(1220227200);
        }
    }

    /**
     * loads dl options from BBC
     * @param context
     * @return bundle hplding download options
     * @throws IOException
     */
    public synchronized Bundle getCurrentDownloadOptions(Context context)  {

        if(currentDownloadOptions!=null && isWithinTimeFrame(timeStampOfcurrentDownloadOptions)) {
            return currentDownloadOptions;
        }
        if(!isDeviceConnected(context))
            return null;
        currentDownloadOptions = new Bundle();
        timeStampOfcurrentDownloadOptions = new Date();

        //MFRI jsoup rein
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.bbc.co.uk/programmes/p002vsnk/episodes/downloads").userAgent(userAgent).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String theQuality = prefs.getString("dl_qual","Lower quality (64kbps)");
            if (theElements.get(i).text().startsWith(theQuality)) {
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
        return timeStampOfcurrentDownloadOptions.getTime() > cal.getTime().getTime();

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
            assert date != null;
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

        bundle.putString("fileName", item.fileName);
        bundle.putBoolean("isToastOnFileExists", isToastOnFileExists);
        bundle.putBoolean("isStartedInBackground", isStartedInBackground);
        Intent intent = new Intent(theContext, DownloadService.class);
        intent.putExtras(bundle);
        return intent;

    }

    /**
     * @param theContentDesc
     * @param theDate
     * @return
     */
    private String prepareFilename(String theContentDesc, String theDate) {
        theDate = theDate.replace(",", "");
        theDate = theDate.replaceAll(" ", "_");
        theDate = theDate.replaceAll(":", "_");
        theContentDesc = theContentDesc.replace("?", "");
        theContentDesc = theContentDesc.replaceAll(" ", "_");
        theContentDesc = theContentDesc.replaceAll(":", "_");
        theContentDesc = replaceInName(theContentDesc, "'", "_");
        theContentDesc = replaceInName(theContentDesc, "\"", "");

        return "Newshour_"+theContentDesc+"_"+theDate+".mp3";
    }

    @NonNull
    private String replaceInName(String theContentDesc, String s, String s2) {
        for (int i = 0; i < 10; i++) {
            if (theContentDesc.indexOf(s) == -1)
                break;
            theContentDesc = theContentDesc.replace(s, s2);
        }
        return theContentDesc;
    }


    /**
     * Check for existance of given filename in podcasts directory
     *
     * @param fileName
     * @return
     */
    public File fileExists(String fileName, Context context) {
        String root = PreferenceManager.getDefaultSharedPreferences(context).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
        File myDir = new File(root );
        if (!myDir.exists()) {
            return null;
        }
        File theFile = new File(root +"/" + fileName);
        if (theFile.exists()) {
            return theFile;
        }
        return null;
    }
    public void showNotification(String title, String message, Context context, NotificationManager mNotificationManager) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1234567",
                    "BBC_POD_DL",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Download podcasts ongoing");
            mNotificationManager.createNotificationChannel(channel);
        }
        // Create an explicit intent for an SettingsActivity => Entrypoint of the app
        Intent intent = new Intent(context, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1234567")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // clear notification after click
        mNotificationManager.notify(0, mBuilder.build());
    }
    /*
    * Method to check storage options
    * ContextCompat.getExternalFilesDirs(context, null) gives an array of File
    * position [0] always points to the internal root dir
    *
    * */
    public static CharSequence[] getStoragePaths(Context context) {

            if(ContextCompat.getExternalFilesDirs(context, null).length >=2 )
                return addToDirArry(2, ContextCompat.getExternalFilesDirs(context, null));
            else
                return addToDirArry(1, ContextCompat.getExternalFilesDirs(context, null));


    }
    private static String[] addToDirArry(int count, File[] fileArry){
        List<String> resultList = new LinkedList<String>();
        for(int i=0; i < count;i++){
            //int endIndex = fileArry[i].getAbsolutePath().indexOf("Android/data/");
            //String rootDir = null;
            //if(endIndex!=-1)
            //    rootDir = fileArry[i].getAbsolutePath().substring(0,fileArry[i].getAbsolutePath().indexOf("Android/data/"));
            //else
            //    rootDir = fileArry[i].getAbsolutePath();
            //resultList.add(rootDir);
            resultList.add(fileArry[i].getAbsolutePath());
        }
        String[] resultArry = new String[resultList.size()];
        for(int i=0;i<resultList.size();i++)
            resultArry[i] = resultList.get(i);
        return resultArry;

    }

    public Intent getSettingsIntend(Context context) {
        Intent intent_settings = new Intent(context, SettingsActivity.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("show_settings", true).apply();
        editor.commit();
        return intent_settings;
    }

    @Nullable
    public Bundle getDownloadOptionsBundle(Context context){

        Bundle theDownloadedPodcastBundle;
        try {
            theDownloadedPodcastBundle = this.getDownloadedPodcastsBundle(context);
        } catch (IOException e) {
            e.printStackTrace();
            theDownloadedPodcastBundle = null;
        }
        Bundle downloadOptionsBundle = this.getCurrentDownloadOptions(context);


        if(theDownloadedPodcastBundle!=null){
            if(downloadOptionsBundle!=null)
                mergeBundles(theDownloadedPodcastBundle, downloadOptionsBundle);
            else
                //Just use already downloaded podcasts if not connected to internet
                downloadOptionsBundle = theDownloadedPodcastBundle;
        }
        return downloadOptionsBundle;
    }

    /**
     * merge fresh download options with already downloaded item options
     * @param theDownloadedPodcastBundle
     * @param downloadOptionsBundle
     */
    private void mergeBundles(Bundle theDownloadedPodcastBundle, Bundle downloadOptionsBundle) {
        int theSizeOfDownloadOptions = downloadOptionsBundle.getInt("LIST_SIZE");
        int theSizeOfDownloadedPodcasts = theDownloadedPodcastBundle.getInt("LIST_SIZE");
        int sizeAll = theSizeOfDownloadOptions;

        for (int i=0;i<theSizeOfDownloadedPodcasts;i++){
            DownloadListItem item = theDownloadedPodcastBundle.getParcelable("ITEM_"+i);

            boolean isFound = false;
            for (int j=0;j<theSizeOfDownloadOptions;j++){
                DownloadListItem itemOptions = downloadOptionsBundle.getParcelable("ITEM_"+j);
                if(item.fileName!=null && itemOptions.fileName!=null)
                    if(item.fileName.equals(itemOptions.fileName)){
                        downloadOptionsBundle.remove("ITEM_" + j);
                        //Neuer ITEM, weil url final ist, somit nicht auf "none" gesetzt werden kann, also ersetzen
                        //DownloadListItem itemTemp = new DownloadListItem(String.valueOf(j), itemOptions.content, "none", itemOptions.dateOfPublication, itemOptions.fileName);
                        downloadOptionsBundle.putParcelable("ITEM_" + j, item);

                        isFound = true;
                        break;
                    }
            }
            //Nicht gefunden, anhaengen an original download optionss
            if(isFound==false){
                sizeAll++;
                downloadOptionsBundle.putParcelable("ITEM_"+sizeAll, item);
            }
        }
        downloadOptionsBundle.remove("LIST_SIZE");
        sizeAll++;
        downloadOptionsBundle.putInt("LIST_SIZE",sizeAll);
    }

    public static void checkDir(File myDir, Context context) {
        if (!myDir.exists()) {
            if(!myDir.mkdirs()){
                Toast.makeText(context, "Directory "+ myDir +" not created, switch directory settings to internal storage", Toast.LENGTH_LONG).show();
            }
        }
    }
}
