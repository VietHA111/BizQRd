package com.example.BizQRd.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import com.example.BizQRd.activities.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezvcard.parameter.AddressType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Nickname;

import static java.lang.Boolean.FALSE;

public class Contact {
    private static final String SELECTION = ContactsContract.Data.LOOKUP_KEY + " = ?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?";

    public static String KEY_GIVEN = "KEY_GIVEN";
    public static String KEY_MIDDLE = "KEY_MIDDLE";
    public static String KEY_FAMILY = "KEY_FAMILY";
    public static String KEY_SUFFIX = "KEY_SUFFIX";
    public static String KEY_PREFIX = "KEY_PREFIX";

    public static String KEY_ORG = "KEY_ORG";
    public static String KEY_DEPT = "KEY_DEPT";
    public static String KEY_TITLE = "KEY_TITLE";

    private final Uri uriContact;
    private final Context mContext;
    private final VCardGenerator vcg;
    private String lookupKey;

    public Contact(Uri uriContact, Context mContext) {
        this.uriContact = uriContact;
        this.mContext = mContext;
        vcg = new VCardGenerator(this, mContext);
        getLookupKey();
    }

    private void getLookupKey() {
        Cursor cursor = mContext.getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts.LOOKUP_KEY},
                null, null, null);
        if (cursor.moveToFirst()) {
            lookupKey = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.LOOKUP_KEY));
        }
        cursor.close();
    }

    public Bitmap generateQRCode() throws Exception {
        String vCard = vcg.generateVCard();
        return QRCodeGenerator.generateQRCode(vCard);
    }

    public String getFirstLastName() {
        String firstLastName = "";
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Map<String, String> nameInfoMap = getNameInfo();
        if (nameInfoMap.containsKey(KEY_GIVEN)) {
            firstLastName += nameInfoMap.get(KEY_GIVEN);
        }
        if (nameInfoMap.containsKey(KEY_FAMILY)) {
            firstLastName += " " + nameInfoMap.get(KEY_FAMILY);
        }
        return firstLastName;
    }

    protected List<Pair<String, TelephoneType>> getPhoneNumbers() {
        String[] phoneSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        Cursor phoneCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, phoneSelectionArgs, null);
        List<Pair<String, TelephoneType>> phoneNumbers = new ArrayList<>();
        while (phoneCur.moveToNext()) {
            String contactNumber = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            TelephoneType numberType = convertIntToTelephone(phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
            Pair<String, TelephoneType> phoneNumber = new Pair<>(contactNumber, numberType);
            phoneNumbers.add(phoneNumber);
        }
        phoneCur.close();
        return phoneNumbers;
    }

    protected Map<String, String> getNameInfo() {
        Map<String, String> nameInfoMap = new HashMap<>();

        String[] nameSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor nameCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nameSelectionArgs, null);

        if (nameCur.moveToFirst()) {

            String fam = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            if (fam != null) {
                nameInfoMap.put(KEY_FAMILY, fam);
            }

            String giv = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            if (giv != null) {
                nameInfoMap.put(KEY_GIVEN, giv);
            }

            String mid = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
            if (mid != null) {
                nameInfoMap.put(KEY_MIDDLE, mid);
            }

            String pref = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
            if (pref != null) {
                nameInfoMap.put(KEY_PREFIX, pref);
            }

            String suf = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
            if (suf != null) {
                nameInfoMap.put(KEY_SUFFIX, suf);
            }
        }

        nameCur.close();
        return nameInfoMap;
    }

    protected Nickname getNickname() {
        Nickname n = new Nickname();
        String[] nickSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
        Cursor nickCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nickSelectionArgs, null);
        while (nickCur.moveToNext()) {
            String nick = nickCur.getString(nickCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
            if (nick != null) {
                n.getValues().add(nick);
            }
        }
        nickCur.close();
        return n;
    }

    protected Map<String, String> getJobInfo() {
        String [] jobSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Map<String, String> jobInfoMap = new HashMap<>();
        Cursor jobCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, jobSelectionArgs, null);
        if (jobCur.moveToFirst()) {
            String company = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
            if (company != null) {
                jobInfoMap.put(KEY_ORG, company);
            }

            String dep = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
            if (dep != null) {
                jobInfoMap.put(KEY_DEPT, dep);
            }


            String job = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
            if (job != null) {
                jobInfoMap.put(KEY_TITLE, job);
            }
        }
        jobCur.close();
        return jobInfoMap;
    }

    protected String getNotes() {
        String [] notesSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        String notes = null;
        Cursor noteCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, notesSelectionArgs, null);
        if (noteCur.moveToFirst()) {
            notes = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
        }
        noteCur.close();
        return notes;
    }

    protected List<Address> getPostalAddresses() {
        List<Address> addresses = new ArrayList<>();
        String [] addressSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
        Cursor addCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, addressSelectionArgs, null);
        while(addCur.moveToNext()) {
            Address address = new Address();
            int type = addCur.getInt(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            address.getTypes().add(convertIntToAddressType(type));
            String poBox = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));

            if (poBox != null) {
                address.setPoBox(poBox);
            }
            String streetAddress = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            if (streetAddress != null) {
                address.setStreetAddress(streetAddress);
            }
            String city = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            if (city != null) {
                address.setLocality(city);
            }
            String region = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
            if (region != null) {
                address.setRegion(region);
            }
            String postCode = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
            if (postCode != null) {
                address.setPostalCode(postCode);
            }
            String country = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            if (country != null) {
                address.setCountry(country);
            }
            addresses.add(address);
        }
        addCur.close();
        return addresses;
    }

    private AddressType convertIntToAddressType(int i) {
        switch (i) {
            case 1:
                return AddressType.HOME;
            case 2:
                return AddressType.WORK;
            default:
                return AddressType.PREF;
        }
    }

    private TelephoneType convertIntToTelephone(int i) {
        switch (i) {
            case 1:
                return TelephoneType.HOME;
            case 3:
                return TelephoneType.WORK;
            default:
                return TelephoneType.CELL;
        }
    }
}
