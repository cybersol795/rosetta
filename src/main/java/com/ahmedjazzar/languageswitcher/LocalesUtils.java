package com.ahmedjazzar.languageswitcher;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * This class is a helper class that connects all library classes activities together and make it
 * easier for every class in the library to use and look at the shared info without a need to
 * initialize a new object from the desired class
 *
 * Created by ahmedjazzar on 1/19/16.
 */
final class LocalesUtils {

    private static LocalesDetector sDetector;
    private static LocalesPreferenceManager sLocalesPreferenceManager;
    private static HashSet<Locale> sLocales;
    private static final Locale[] PSEUDO_LOCALES = {
            new Locale("en", "XA"),
            new Locale("ar", "XB")
    };
    private static final String TAG = LocalesDetector.class.getName();
    private static Logger sLogger = new Logger(TAG);

    /**
     *
     * @param detector just a setter because I don't want to declare any constructors in this class
     */
    static void setDetector(@NonNull LocalesDetector detector) {
        LocalesUtils.sDetector = detector;
    }

    /**
     *
     * @param localesPreferenceManager just a setter because I don't want to declare any
     *                                 constructors in this class
     */
    static void setLocalesPreferenceManager(
            @NonNull LocalesPreferenceManager localesPreferenceManager)   {

        LocalesUtils.sLocalesPreferenceManager = localesPreferenceManager;
    }

    /**
     *
     * @param stringId a string to start discovering sLocales in
     * @return a HashSet of discovered sLocales
     */
    static HashSet<Locale> fetchAvailableLocales(int stringId)  {
        return sDetector.fetchAvailableLocales(stringId);
    }

    /**
     *
     * @param localesSet sLocales  user wanna use
     */
    static void setSupportedLocales(HashSet<Locale> localesSet)    {
        LocalesUtils.sLocales = sDetector.validateLocales(localesSet);
        sLogger.debug("Locales have been changed");
    }

    /**
     *
     * @return a HashSet of the available sLocales discovered in the application
     */
    static HashSet<Locale> getLocales()    {
        return LocalesUtils.sLocales;
    }

    /**
     *
     * @return a list of locales for displaying on the layout purposes
     */
    static ArrayList<String> getLocalesWithDisplayName()   {
        ArrayList<String> stringLocales = new ArrayList<>();

        for (Locale loc: LocalesUtils.getLocales()) {
            stringLocales.add(loc.getDisplayName(loc));
        }
        return stringLocales;
    }

    /**
     *
     * @return the index of the current app locale
     */
    static int getCurrentLocaleIndex()    {
        Locale locale = LocalesUtils.getCurrentLocale();
        int index = -1;
        int itr = 0;

        for (Locale l : sLocales)  {
            if(locale.equals(l))    {
                index = itr;
                break;
            }
            itr++;
        }

        if (index == -1)    {
            //TODO: change the index to the most closer available locale
            sLogger.warn("Current device locale '" + locale.toString() +
                    "' does not appear in your given supported locales");

            index = sDetector.detectMostClosestLocale(locale);
            if(index == -1)   {
                index = 0;
                sLogger.warn("Current locale index changed to 0 as the current locale '" +
                                locale.toString() +
                                "' not supported."
                );
            }
        }

        return index;
    }

    /**
     *
     * @see <a href="http://en.wikipedia.org/wiki/Pseudolocalization">Pseudolocalization</a> for
     *      more information about pseudo localization
     * @return pseudo locales list
     */
    static List<Locale> getPseudoLocales()  {
        return Arrays.asList(LocalesUtils.PSEUDO_LOCALES);
    }

    /**
     *
     * @param context
     * @param index the selected locale position
     * @return true if the application locale changed
     */
    static boolean setAppLocale(Context context, int index)    {

        Locale newLocale = LocalesUtils.sLocales.toArray(new Locale[LocalesUtils.sLocales.size()])[index];
        return setAppLocale(context, newLocale);
    }

    /**
     *
     * @param context
     * @return true if the application locale changed
     */
    static boolean setAppLocale(Context context, Locale newLocale)    {

        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        Locale oldLocale = new Locale(configuration.locale.getLanguage(), configuration.locale.getCountry());
        configuration.locale = newLocale;
        resources.updateConfiguration(configuration, displayMetrics);

        if(oldLocale.equals(newLocale)) {
            return false;
        }

        if (LocalesUtils.updatePreferredLocale(newLocale))    {
            sLogger.info("Locale preferences updated to: " +
                    LocalesUtils.sLocalesPreferenceManager.getPreferredLocale());
        } else  {
            sLogger.error("Failed to update locale preferences.");
        }

        return true;
    }

    /**
     *
     * @return application's base locale
     */
    static Locale getBaseLocale()    {
        return LocalesUtils.sLocalesPreferenceManager.getBaseLocale();
    }

    /**
     *
     * @param locale the new preferred locale
     * @return true if the preferred locale updated
     */
    private static boolean updatePreferredLocale(Locale locale)    {

        return LocalesUtils.sLocalesPreferenceManager
                .setPreferredLocale(locale);
    }

    /**
     *
     * @return current application locale
     */
    private static Locale getCurrentLocale() {
        return sDetector.getCurrentLocale();
    }
}