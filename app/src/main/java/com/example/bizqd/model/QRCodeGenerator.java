package com.example.bizqd.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.bizqd.activities.MainActivity;

import net.glxn.qrgen.android.QRCode;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Nickname;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;

public class QRCodeGenerator {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SELECTION = ContactsContract.Data.LOOKUP_KEY + " = ?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?";

    private Uri uriContact;
    private String lookupKey;
    private Context mContext;
    private boolean[] settings;

    public QRCodeGenerator(Uri uriContact, Context mContext, boolean[] settings) {
        this.uriContact = uriContact;
        this.mContext = mContext;
        this.settings = settings;
    }

    public Bitmap generateQRCode() {
        String str = formVCard();
        for (int i = 0; i < 10; i++) {
            System.out.println(settings[i]);
        }


        return QRCode.from(str).bitmap();
    }

    private String retrieveContactNumber(String v) {

        String[] phoneSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        Cursor phoneCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, phoneSelectionArgs, null);
        while (phoneCur.moveToNext()) {
//            contactNumber = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            numberType = phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            v += "TEL;TYPE=" + convertIntToTelephone(phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))) + ";VALUE=uri:" + phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + "\n";
        }
        return v;
    }

    private String retrieveContactName(String v) {
        v += "N:";

        String[] nameSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor nameCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nameSelectionArgs, null);

        if (nameCur.moveToFirst()) {
            if (settings[2]) {
                String fam = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                if (fam != null) {
                    v += fam;
                }
            }
            v += ";";
            if (settings[1]) {
                String giv = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                if (giv != null) {
                    v += giv;
                }
            }
            v += ";";
            if (settings[3]) {
                String mid = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                if (mid != null) {
                    v += mid;
                }
            }
            v += ";";
            if (settings[0]) {
                String pref = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
                if (pref != null) {
                    v += pref;
                }
            }
            v += ";";
            if (settings[4]) {
                String suf = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
                if (suf != null) {
                    v += suf;
                }
            }
            v += "\n";
        }
        nameCur.close();
        return v;
    }

    private String getNickname(String v) {
        v += "NICKNAME:";
        String[] nickSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
        Cursor nickCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nickSelectionArgs, null);
        boolean first = true;
        while (nickCur.moveToNext()) {
            if (!first) {
                v += ",";
            } else {
                first = false;
            }
            String nick = nickCur.getString(nickCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
            if (nick != null) {
                v += nick;
            }
        }
        v += "\n";
        nickCur.close();
        return v;
    }

    private String setJobInfo(String v) {

        String [] jobSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};

        Cursor jobCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, jobSelectionArgs, null);
        if (jobCur.moveToFirst()) {
            v += "ORG:";
            if (settings[6]) {
                String company = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                if (company != null) {
                    v += company;
                }
            }
            v += ";";
            if (settings[7]) {
                String dep = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
                if (dep != null) {
                    v += dep;
                }
            }
            v += "\n";
            if (settings[8]) {
                String job = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                if (job != null) {
                    v += "TITLE:" + job + "\n";
                }
            }
        }
        jobCur.close();
        return v;
    }

    private String setPostalAddress(String v) {
        String [] addressSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};

        Cursor addCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, addressSelectionArgs, null);
        while (addCur.moveToNext()) {
            int type = addCur.getInt(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
            v += "ADR;TYPE=" + getTypeString(type) + ":";
            String poBox = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
            if (poBox != null) {
                v += poBox;
            }
            v += ";;";
            String streetAddress = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
            if (streetAddress != null) {
                v += streetAddress + ";";
            }
            String city = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
            if (city != null) {
                v += city + ";";
            }
            String region = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
            if (region != null) {
                v += region + ";";
            }
            String postCode = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
            if (postCode != null) {
                v += postCode + ";";
            }
            String country = addCur.getString(addCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
            if (country != null) {
                v += country + "\n";
            }
        }
        addCur.close();
        return v;
    }

    private String formVCard() {
        getLookupKey();
        String vCard = "BEGIN:VCARD\n" +
                "VERSION:4.0\n";

        vCard = retrieveContactNumber(vCard);

        vCard = retrieveContactName(vCard);

        if (settings[5]) {
            vCard = getNickname(vCard);
        }

        vCard = setJobInfo(vCard);

        if (settings[9]) {
            vCard = setPostalAddress(vCard);
        }

        vCard += "END:VCARD\n";
        return vCard;
    }

    private String convertIntToTelephone(int i) {
        switch (i) {
            case 1:
                return "Home";
            case 3:
                return "Work";
            default:
                return "Mobile";
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
    }

    private String getTypeString(int i) {
        if (i == 2) {
            return "work";
        } else {
            return "home";
        }
    }
}
