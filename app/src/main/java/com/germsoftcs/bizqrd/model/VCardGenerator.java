package com.germsoftcs.bizqrd.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import com.germsoftcs.bizqrd.activities.SettingsActivity;

import java.util.List;
import java.util.Map;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Nickname;
import ezvcard.property.Organization;
import ezvcard.property.StructuredName;

import static java.lang.Boolean.FALSE;

public class VCardGenerator {

    final SharedPreferences sharedPref;
    private final Contact contact;

    public VCardGenerator(Contact contact, Context mContext) {
        this.contact = contact;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    //EFFECTS: return contact's VCard string
    public String generateVCard() {
        VCard vcard = formVCard();

        return Ezvcard.write(vcard).version(VCardVersion.V4_0).go();
    }

    //EFFECTS: return contact's VCard object
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

        if (sharedPref.getBoolean(SettingsActivity.KEY_EMAIL, FALSE)) {
            setEmail(vCard);
        }

        return vCard;
    }

    //EFFECTS: add phone numbers to VCard vCard
    private void setContactNumber(VCard v) {
        List<Pair<String, TelephoneType>> phoneNumbers = contact.getPhoneNumbers();

        for (int i = 0; i < phoneNumbers.size(); i++) {
            v.addTelephoneNumber(phoneNumbers.get(i).first, phoneNumbers.get(i).second);
        }
    }

    //EFFECTS: add wanted names to Vcard v
    private void setContactName(VCard v) {
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

        v.setStructuredName(n);
    }

    //EFFECTS: add nickname to VCard v
    private void setNickname(VCard v) {
        Nickname n = contact.getNickname();
        v.addNickname(n);
    }

    //EFFECTS: add wanted job info to VCard v
    private void setJobInfo(VCard v) {

        Map<String, String> jobInfoMap = contact.getJobInfo();

        Organization org = new Organization();

        if (sharedPref.getBoolean(SettingsActivity.KEY_ORG, FALSE) || sharedPref.getBoolean(SettingsActivity.KEY_DEPT, FALSE)) {
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
        }

        if (sharedPref.getBoolean(SettingsActivity.KEY_JOB, FALSE)) {
            if (jobInfoMap.containsKey(Contact.KEY_TITLE)) {
                v.addTitle(jobInfoMap.get(Contact.KEY_TITLE));
            }
        }
    }

    //EFFECTS: add addresses to VCard v
    private void setPostalAddress(VCard v) {
        List<Address> addresses = contact.getPostalAddresses();
        for (int i = 0; i < addresses.size(); i++) {
            v.addAddress(addresses.get(i));
        }
    }

    //EFFECTS: add notes to VCard v
    private void setNotes(VCard vCard) {
        String notes = contact.getNotes();
        if (notes != null) {
            vCard.addNote(notes);
        }
    }

    //EFFECTS: add email to VCard v
    private void setEmail(VCard v) {
        String email = contact.getEmail();
        if (email != null) {
            v.addEmail(email);
        }
    }
}
