package com.example.vbus.notification

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

object AccessToken {
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken():String?{
        try{
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"vbus-160e8\",\n" +
                    "  \"private_key_id\": \"a771b41d2f030594ab50a8cc4f64b77a8d2fd141\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7UQpUjOSHjSBa\\n3Pf2bDTUslrHulaVOLFDbRrWhhn8Q4WXoXzoTJJeZMvn1t8kafiWT/omMKSuJqWn\\nrnsZTJNNSc4SMLY9DLk1xQB1ihwyCgtpK5bhaJuSoEVKMhrnHHAsMDQilbV3EVlR\\ntNE9/WLntCJfo1jeI/fJCP+Iq5EWXggCyPcL4NcJCgwx3jyWgKN0cY6ufw8V5EYc\\n3VvzFYg3ilFW1CetXcIezP1hr+3BrDKpX7cFgAL1F0XiFSZtjNsZ6tFKAznLRqai\\n0AUBFnULnQiU5l7aUPS0LamtlHTydJ7tMweNscJ4l1lf/eAU1xetdx2mpToYOI/Q\\nnKpn4tdZAgMBAAECggEAFRKZH6lpBTHUd7xfneaO6/WznkABgRCLapsx13vnHPjI\\npOYLLAP/PGTJBqgm2s0cJObS7KxwvlHFNY+8s/woHxwENYoq2kPd6yViN5bltKBq\\nJ7b6ZpnS1KQ4n0/zNFg/60yx9MkUTNSUEIvPtVJV59ydOFajRvxTYhejza+FZzdT\\nwXaWjgXq3oQFbgk80HAv2gTt7jrEQb97LJOeRGFRRwKSDmyQfXoF170sgAgKikJX\\nVRjXbochhLRibDrE4zAmU1/tMzS5VuLKD4CLTtlMmuIfKe3/JEHxHuKkmEYo28je\\nPG6NsRB/4ud74bFJcCOxbids51PGOSzcnC5/fSIygQKBgQD2SCzYgCklMApBcNW8\\n03iG6qtOJZYOJayLcCmWpNSiRz6FmgxxqvyI3d4El8y2nKLMmTqbc5e+xcgAFi3O\\nY0pxdMCL/1xsrb1R4u6O5PhMAoAPOtrjXBm4xVDhHrTLwqzD7rpn3jBelZaoeYuf\\n77hYarhT04+CeVme2FUiFU2uGQKBgQDCtTl/jDsNsxYInC/WDLrlYhF2cdAZdeiD\\nOsJOuEjGVHDgi4fiqTQQjvr1y50S1PiJqhbwn3AtV9fDH41W1/ipZQ8HPyTKQ6p5\\nuj5MZzgBC92hRIvL+IpCfjAFxJtFOP3bWSOKcKR4Yo9mKaMXg3uF6chccKYv3cAS\\nYUj569UbQQKBgQCVr1hSXdKkH4acL5A4q+7dUVO8s2t4Pb2ZIhqkzJnP561AVlip\\n3VqPdQmzkO+DLcBkydytDpxz/pGIO/KrNf3Q5zz/2by68P3X/y0u/EtNJ1fd6c+P\\n0DJx8rstmBQao7+NdlMlPg337sMasoUCOBi65GIT0Mmwa0DlImbbyEbmQQKBgG7t\\nQkG5SZeHdMEZAmPHQt8WHO7G7pDizMGewz6H3OYpC1nSBGuMjF32p2FLTESDUslT\\nPth9bTJX/lNq8WINjtwq+AHf5nzZShEpmv56O7zU3sJWw/JDubkiHQfcN72bN300\\n7nubqwfu1tWUWRc3UDM9uml3wCY7BocyJ5pC0llBAoGACsQUrQGuUwGHK25zVF5N\\nkYvWhYIzQyZnAKVum8oTKavmTJLUX32kwozcMZsW+uULwtCFuxH9lNtiNv39ukl+\\nbNDup73ONwTEvhsxWjtXJqLDbDslqxv//eTQjpxSgiLij3WAbmFCD5pHHOrFos9C\\nlqxTWHBeoLNz05dpvBl/4FI=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-xlr70@vbus-160e8.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"100765683960166025683\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xlr70%40vbus-160e8.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n"

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))

            googleCredential.refresh()
            return googleCredential.accessToken.tokenValue
        }
        catch (e:Exception){
            return null
        }
    }
}