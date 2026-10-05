# Privacy Policy

**Effective date:** 5 October 2026

This Privacy Policy describes how SmsMan ("the app") handles information. SmsMan is an open-source project maintained by AbrarShakhi ("the developer", "we", "us" or "our"). This policy applies to the app as published in the project's official repository and releases.

## Summary

- SmsMan does not collect, transmit, sell or share your personal information.
- The app has no user accounts, advertising, analytics or crash reporting, and it does not request permission to access the Internet.
- Your messages are stored by Android on your device. The developer has no access to them.

## Information the app accesses

To act as your SMS app, SmsMan accesses the following information on your device. This information is processed only on your device and is never sent to the developer.

- **Messages** (`READ_SMS`, `RECEIVE_SMS`, `SEND_SMS`): the text of your messages, the addresses of senders and recipients, timestamps, read and delivery status, and the SIM used. SmsMan reads and writes this information in Android's system message store so that you can view, send, receive and delete messages.
- **Multimedia messages** (`RECEIVE_MMS`, `RECEIVE_WAP_PUSH`): Android requires the default SMS app to hold these permissions. SmsMan does not currently download or store multimedia messages.
- **Contacts** (`READ_CONTACTS`): contact names and phone numbers, used to identify the people in your conversations and to suggest recipients.
- **Phone state** (`READ_PHONE_STATE`): the names, carriers and slot numbers of your SIM cards, used to let you choose the SIM that sends a message.
- **Notifications** (`POST_NOTIFICATIONS`): used to alert you to new messages.

## Information the app stores

In addition to the system message store, SmsMan keeps a small amount of information in its private storage on your device:

- **Preferences:** your theme, colour and font choices.
- **Favourites:** the identifiers of conversations that you mark as favourite.
- **Pins:** the identifiers of messages that you pin, together with a one-way fingerprint used to recognise them. The text of pinned messages is not copied.

## Disclosure of information

The developer does not receive any information from the app. Information leaves your device only in the following circumstances:

- **Sending messages.** Messages that you send are delivered by your mobile network operator, which processes them under its own terms and privacy policy.
- **Optional fonts.** If you select a font other than the system default, SmsMan requests that font by name from the Google Fonts provider in Google Play services, which may download it from Google. The request does not include personal information. Google's privacy policy applies to this service.
- **Device backups.** If backup is enabled on your device, Android may include SmsMan's preferences, favourites and pins, as well as the system message store, in backups managed by Google or by your device manufacturer. These backups are governed by the backup provider's terms.
- **External links.** Links to the project's website, such as its GitHub repository, open in your browser. The privacy policy of the website that you visit applies.

Message notifications may show sender names and message text on your screen, including the lock screen, depending on your device's notification settings.

## Retention and deletion

- Messages remain in the system message store until you delete them. Deleting a message or conversation in SmsMan removes it from the system message store.
- Clearing SmsMan's storage or uninstalling the app removes its preferences, favourites and pins. It does not remove your messages, which remain available to other messaging apps.

## Security

SmsMan relies on Android's application sandbox and permission system to protect the information on your device. We recommend that you protect your device with a screen lock and keep its software up to date.

## Your choices

You can review and revoke SmsMan's permissions at any time in your device's settings. If you revoke the required permissions or choose another default SMS app, SmsMan can no longer send, receive or display messages.

## Children's privacy

SmsMan is a general-purpose communication tool and is not directed at children. Because the app does not collect personal information, it does not knowingly collect information from children.

## Changes to this policy

We may update this Privacy Policy from time to time. Changes are published in the project repository, and the effective date above is updated accordingly. Each release of the app includes the version of this policy that applies to it.

## Contact

If you have questions about this Privacy Policy, please open an issue at <https://github.com/AbrarShakhi/smsman/issues>.
