package com.example.BizQRd.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.preference.PreferenceManager;

import com.example.BizQRd.activities.SettingsActivity;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Nickname;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;

import static java.lang.Boolean.FALSE;

public class VCardGenerator {
    private static final String SELECTION = ContactsContract.Data.LOOKUP_KEY + " = ?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?";
    SharedPreferences sharedPref;

    private String firstLastName;
    private final Uri uriContact;
    private String lookupKey;
    private final Context mContext;
    private final boolean[] settings;

    public VCardGenerator(Uri uriContact, Context mContext, boolean[] settings) {
        this.uriContact = uriContact;
        this.mContext = mContext;
        this.settings = settings;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getFirstLastName() {
        return firstLastName;
    }

    public String generateVCard() {
        VCard v = formVCard();

        return Ezvcard.write(v).version(VCardVersion.V4_0).go();
    }

    private VCard formVCard() {
        getLookupKey();
        VCard vCard = new VCard();

        if (sharedPref.getBoolean(SettingsActivity.KEY_POSTAL, FALSE)) {
            setPostalAddress(vCard);
        }

        retrieveContactNumber(vCard);

        StructuredName name = retrieveContactName();

        vCard.setStructuredName(name);

        if (sharedPref.getBoolean(SettingsActivity.KEY_POSTAL, FALSE)) {
            getNickname(vCard);
        }

        setJobInfo(vCard);

        if (sharedPref.getBoolean(SettingsActivity.KEY_POSTAL, FALSE)) {
            setNotes(vCard);
        }

        return vCard;
    }

    private void retrieveContactNumber(VCard v) {

        String[] phoneSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        Cursor phoneCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, phoneSelectionArgs, null);
        while (phoneCur.moveToNext()) {
//            contactNumber = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            numberType = phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            v.addTelephoneNumber(phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), convertIntToTelephone(phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))));
        }
        phoneCur.close();
    }

    private StructuredName retrieveContactName() {
        StructuredName n = new StructuredName();

        String[] nameSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor nameCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nameSelectionArgs, null);
        String firstName = "";
        String lastName = "";

        if (nameCur.moveToFirst()) {
            if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_FAMILY, FALSE)) {
                String fam = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                if (fam != null) {
                    n.setFamily(fam);
                    lastName = fam;
                }
            }
            if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_GIVEN, FALSE)) {
                String giv = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                if (giv != null) {
                    n.setGiven(giv);
                    firstName = giv;
                }
            }
            if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_MIDDLE, FALSE)) {
                String mid = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                if (mid != null) {
                    n.getAdditionalNames().add(mid);
                }
            }
            if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_PREF, FALSE)) {
                String pref = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
                if (pref != null) {
                    n.getPrefixes().add(pref);
                }
            }
            if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_SUFF, FALSE)) {
                String suf = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
                if (suf != null) {
                    n.getSuffixes().add(suf);
                }
            }
        }
        nameCur.close();
        firstLastName = firstName + " " + lastName;
        return n;
    }

    private void getNickname(VCard v) {
        Nickname n = new Nickname();
        String[] nickSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
        Cursor nickCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nickSelectionArgs, null);
        while (nickCur.moveToNext()) {
            String nick = nickCur.getString(nickCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
            if (nick != null) {
                n.getValues().add(nick);
            }
        }
        v.addNickname(n);
        nickCur.close();
    }

    private void setJobInfo(VCard v) {

        String [] jobSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};

        Cursor jobCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, jobSelectionArgs, null);
        if (jobCur.moveToFirst()) {
            Organization org = new Organization();
            if (sharedPref.getBoolean(SettingsActivity.KEY_ORG, FALSE)) {
                String company = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                if (company != null) {
                    org.getValues().add(company);
                }
            }
            if (sharedPref.getBoolean(SettingsActivity.KEY_DEPT, FALSE)) {
                String dep = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
                if (dep != null) {
                    org.getValues().add(dep);
                }
            }
            v.setOrganization(org);
            if (sharedPref.getBoolean(SettingsActivity.KEY_JOB, FALSE)) {
                String job = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                if (job != null) {
                    v.addTitle(job);
                }
            }
        }
        jobCur.close();
    }

    private void setPostalAddress(VCard v) {
        String [] addressSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};

        Cursor addCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, addressSelectionArgs, null);
        while (addCur.moveToNext()) {
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
            v.addAddress(address);
        }
        addCur.close();
    }


    private void setNotes(VCard vCard) {
        String [] notesSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};

        Cursor noteCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, notesSelectionArgs, null);
        if (noteCur.moveToFirst()) {
            String notes = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
            if (notes != null) {
                vCard.addNote(notes);
            }
        }
        noteCur.close();
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

}
