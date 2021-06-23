package com.example.BizQRd.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import com.example.BizQRd.activities.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    SharedPreferences sharedPref;
    private Context mContext;
    private Contact contact;

    public VCardGenerator(Contact contact, Context mContext) {
        this.mContext = mContext;
        this.contact = contact;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String generateVCard() {
        VCard vcard = formVCard();

        return Ezvcard.write(vcard).version(VCardVersion.V4_0).go();
    }

    private VCard formVCard() {
        VCard vCard = new VCard();

        if (sharedPref.getBoolean(SettingsActivity.KEY_POSTAL, FALSE)) {
            setPostalAddress(vCard);
        }

        setContactNumber(vCard);

        setContactName(vCard);

        if (sharedPref.getBoolean(SettingsActivity.KEY_NICKNAME, FALSE)) {
            setNickname(vCard);
        }

        setJobInfo(vCard);

        if (sharedPref.getBoolean(SettingsActivity.KEY_NOTES, FALSE)) {
            setNotes(vCard);
        }

        return vCard;
    }

    private void setContactNumber(VCard v) {
        List<Pair<String, TelephoneType>> phoneNumbers = contact.getPhoneNumbers();

        for (int i = 0; i < phoneNumbers.size(); i++) {
            v.addTelephoneNumber(phoneNumbers.get(i).first, phoneNumbers.get(i).second);
        }
    }

    private void setContactName(VCard vCard) {
        StructuredName n = new StructuredName();
        Map<String, String> nameInfoMap = contact.getNameInfo();

        if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_FAMILY, FALSE)) {
            if (nameInfoMap.containsKey(Contact.KEY_FAMILY)) {
                n.setFamily(nameInfoMap.get(Contact.KEY_FAMILY));
            }
        }
        if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_GIVEN, FALSE)) {
            if (nameInfoMap.containsKey(Contact.KEY_GIVEN)) {
                n.setGiven(nameInfoMap.get(Contact.KEY_GIVEN));
            }
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_MIDDLE, FALSE)) {
            if (nameInfoMap.containsKey(Contact.KEY_MIDDLE)) {
                n.getAdditionalNames().add(nameInfoMap.get(Contact.KEY_MIDDLE));
            }
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_PREF, FALSE)) {
            if (nameInfoMap.containsKey(Contact.KEY_PREFIX)) {
                n.getPrefixes().add(nameInfoMap.get(Contact.KEY_PREFIX));
            }
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_NAME_SUFF, FALSE)) {
            if (nameInfoMap.containsKey(Contact.KEY_SUFFIX)) {
                n.getSuffixes().add(nameInfoMap.get(Contact.KEY_SUFFIX));
            }
        }

        vCard.setStructuredName(n);
    }

    private void setNickname(VCard v) {
        Nickname n = contact.getNickname();
        v.addNickname(n);
    }

    private void setJobInfo(VCard v) {

        Map<String, String> jobInfoMap = contact.getJobInfo();

        Organization org = new Organization();
        if (sharedPref.getBoolean(SettingsActivity.KEY_ORG, FALSE)) {
            if (jobInfoMap.containsKey(Contact.KEY_ORG)) {
                org.getValues().add(jobInfoMap.get(Contact.KEY_ORG));
            }
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_DEPT, FALSE)) {
            if (jobInfoMap.containsKey(Contact.KEY_DEPT)) {
                org.getValues().add(jobInfoMap.get(Contact.KEY_DEPT));
            }
        }

        v.setOrganization(org);

        if (sharedPref.getBoolean(SettingsActivity.KEY_JOB, FALSE)) {
            if (jobInfoMap.containsKey(Contact.KEY_TITLE)) {
                v.addTitle(jobInfoMap.get(Contact.KEY_TITLE));
            }
        }
    }

    private void setPostalAddress(VCard v) {
        List<Address> addresses = contact.getPostalAddresses();
        for (int i = 0; i < addresses.size(); i++) {
            v.addAddress(addresses.get(i));
        }
    }


    private void setNotes(VCard vCard) {
        String notes = contact.getNotes();
        if (notes != null) {
            vCard.addNote(notes);
        }
    }


}
