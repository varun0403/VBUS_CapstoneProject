A smart mobile application for educational institutions to manage its transportation services effeciently without the interference of admin. 

Developed using Jetpack Compose and used utilised Google Map API for navigation and geofencing logic. Firebase is used in backend as of now, might migrate to cloud in upcoming months

Core Features:-

1) Live Bus Tracker
2) Bus Breakdown Resolution: If any bus becomes inoperable during the commute, a highly sophiticated rule-based algorithm finds other active buses on the same route using check points and alert them about the situation. Those drivers will go to the rescue location based on seat availability in their bus
3) Facial Recognition Attendance System: Uses MTCNN for face detection and FaceNet for recognition to ensure students board the right bus. If student is caught boarding the wrong bus, a penalty will be imposed(partially completed)
4) Complaint and Announcements: Students can post complaints individually to admin and admin will post the announcements in the app
5) Late Arrival e-letter: If the bus arrives late to campus, an e-letter will be generated for those students from the tranport dept. Students can use it to prove their genuiness if facuty do not provide attendance for them.
