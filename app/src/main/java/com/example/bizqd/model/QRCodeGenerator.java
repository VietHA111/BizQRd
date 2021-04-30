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
import ezvcard.parameter.TelephoneType;
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

        return QRCode.from(str).bitmap();
    }

    private void retrieveContactNumber(VCard v) {

        String[] phoneSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
        Cursor phoneCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, phoneSelectionArgs, null);
        while (phoneCur.moveToNext()) {
//            contactNumber = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            numberType = phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            v.addTelephoneNumber(phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), convertIntToTelephone(phoneCur.getInt(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))));
        }

    }

    private StructuredName retrieveContactName() {

        StructuredName contactName = new StructuredName();

        String[] nameSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor nameCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nameSelectionArgs, null);

        while (nameCur.moveToNext()) {
            if (settings[0]) {
                String pref = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
                if (pref != null) {
                    contactName.getPrefixes().add(pref);
                }
            }
            if (settings[1]) {
                String giv = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                if (giv != null) {
                    contactName.setGiven(nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
                }
            }
            if (settings[2]) {
                String fam = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                if (fam != null) {
                    contactName.setFamily(fam);
                }
            }
            if (settings[3]) {
                String mid = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                if (mid != null) {
                    contactName.getAdditionalNames().add(mid);
                }
            }
            if (settings[4]) {
                String suf = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
                if (suf != null) {
                    contactName.getSuffixes().add(nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)));
                }
            }
        }
        nameCur.close();

        return contactName;
    }

    private Nickname getNickname() {
        Nickname nickname = new Nickname();

        String[] nickSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE};
        Cursor nickCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, nickSelectionArgs, null);
        while (nickCur.moveToNext()) {
            String nick = nickCur.getString(nickCur.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
            if (nick != null) {
                nickname.getValues().add(nick);
            }
        }
        return nickname;
    }

    private void setJobInfo(VCard v) {

        String [] jobSelectionArgs = new String[]{lookupKey, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};

        Cursor jobCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, SELECTION, jobSelectionArgs, null);
        if (jobCur.moveToFirst()) {
            if (settings[6]) {
                String company = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                if (company != null) {
                    Organization org = new Organization();
                    org.getValues().add(company);
                    v.setOrganization(org);
                }
            }
            if (settings[7]) {
            }
            if (settings[8]) {
                String job = jobCur.getString(jobCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                if (job != null) {
                    v.addRole(job);
                }
            }
        }
    }

    private String formVCard() {
        getLookupKey();
        VCard vcard = new VCard();

        retrieveContactNumber(vcard);

        StructuredName n = retrieveContactName();
        vcard.setStructuredName(n);

        if (settings[5]) {
            vcard.setNickname(getNickname());
        }

        setJobInfo(vcard);

//        if (settings[9]) {
//            vcard.
//        }

        return Ezvcard.write(vcard).version(VCardVersion.V4_0).go();
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

    private void getLookupKey() {
        Cursor cursor = mContext.getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts.LOOKUP_KEY},
                null, null, null);
        if (cursor.moveToFirst()) {
            lookupKey = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.LOOKUP_KEY));
        }
    }

}
