Campsite
Manage campsite information and details (name, owner, etc.)
Maintain information about location, price, capacity, features etc.
Provide details in the campsite search/view screen
Coordinate availability based on a schedule
Associate lotteries and reservations with the site

User
Admin
Entrant
Campsite Owner
Lottery
Reservation


User
Maintain common profile information (name, email, phone number)
Identify user by unique device ID (passwordless auth)
Maintain notification preferences (opt-in / opt-out)
Manage active user roles and permissions (Entrant, Campsite Owner, Admin)
Display public profile information
Allow updating personal info and deleting profile

Notification
Reservation
Lottery


Entrant
Search and filter campsites by location, date, and number of guests
Scan promotional QR codes to view campsite details
Join and leave waiting lists for active campsite lotteries
Receive notifications when winning or losing a lottery
View available dates and select campsite upon winning lottery
Accept or decline reservation offers
View active and past reservations
Cancel reservations and receive confirmation (notification or downloadable CSV)
Confirm campsite rules and policies before booking
Rate and review visited campsites

User
Campsite
Lottery
WaitingList
Reservation
QRCode
Review
Notification


CampsiteOwner
Register campsite listing on the app
Provide and update campsite details (name, location, map coords, description, amenities, occupancy, price, images, tags)
View details of owned campsites
Generate unique promotional QR code linking to campsite details
List one or multiple campsites in a lottery for a specified period
Set lottery registration period, booking period, winner count, and optional waitlist capacity
View booked dates and calendar availability schedule
View lists of active, past, and cancelled reservations
Reject a reservation (e.g., safety hazard, wildfire warning, refund)
View list of entrants on lottery waitlist
Broadcast notifications to waitlisted entrants
Send direct messages to reserving entrants

User
Campsite
CampsiteSchedule
Lottery
WaitingList
Reservation
Notification
Message
QRCode


Admin
Browse all registered campsites, user profiles, and uploaded images
Remove non-compliant campsites
Remove violating user profiles and campsite owners
Remove inappropriate images from listings or profiles
Review system-wide audit logs of all notifications sent
Review system-wide logs of all messages from owners to reserving entrants
Exercise entrant and campsite owner capabilities using admin profile

User
Campsite
CampsiteImage
Notification
Message
Reservation


Reservation
Maintain information about a confirmed or pending booking
Associate a reservation with an entrant, campsite, and booking schedule
Enforce entrant confirmation of rules and policies prior to reservation
Track the status of the reservation (pending, confirmed, cancelled, rejected, past)
Handle cancellations by entrants and rejections by campsite owners
Provide reservation receipt via notification and downloadable CSV

Entrant
Campsite
Campsite Owner
Admin
CampsiteSchedule
Notification


Lottery
Manage lottery listing parameters (registration period, booking period, winner count)
Associate one or multiple campsites in a lottery pool
Track lottery status (open, closed, drawn, expired)
Manage list of entrants via waiting list
Automatically draw winners randomly when registration period ends
Notify winners and non-selected entrants
Allow selected winners to proceed with date selection and booking

Campsite
Entrant
Admin
WaitingList
Reservation
Notification


WaitingList
Maintain list of entrants for a specific lottery
Add and remove entrants upon request
Confirm new entrants are unique and not already enrolled
Enforce optional waitlist capacity set by owner
Provide entrant pool for random lottery draw
Provide recipient roster for owner notification broadcasts

Lottery
Entrant
Notification


Notification
Encapsulate notification payload (type, message, timestamp, status)
Dispatch lottery win/loss alerts, booking confirmations, and cancellations
Respect user notification opt-out preferences
Maintain an immutable system-wide notification log for admin audit

User
Lottery
Reservation


QRCode
Generate unique scannable QR codes linking to campsite details
Decode scanned QR codes from camera input
Resolve QR code tokens to target campsite records

Campsite
Lottery
Entrant


Review
Store numerical rating (1-5 stars) and feedback for a campsite
Associate review with an entrant and campsite
Compute running average rating for campsite listing

Entrant
Campsite


Message
Encapsulate direct communication between campsite owner and reserving entrant
Store message text, sender ID, recipient ID, reservation ID, and timestamp
Maintain immutable message logs for admin moderation and audit

CampsiteOwner
Entrant
Reservation
Admin


CampsiteImage
Store metadata, storage URL, and upload timestamp for photos
Associate images with campsite listings or user profiles
Support browsing and administrative removal of violating media

Campsite
User
Admin


CampsiteSchedule
Maintain calendar availability schedule set by campsite owner
Track booked dates to prevent double-booking
Validate requested booking dates during reservation confirmation
Release dates upon cancellation or rejection

Campsite
Reservation


MainScreenUI
Display campsite search bar and filter controls (location, dates, guests)
Render list of campsites based on active search/filters
Provide button to launch promotional QR code scanner
Handle navigation to campsite details, profile, or owner views

Campsite
QRCode
CampsiteScreenUI
UserProfileScreenUI


CampsiteScreenUI
Display complete campsite details, amenities, price, occupancy, images, and reviews
Display map view showing campsite geolocation
Display availability calendar and active lottery countdown
Provide buttons to join/leave waiting list or reserve dates (if winner)
Provide owner controls to edit campsite or create lottery listings

Campsite
Lottery
CampsiteSchedule
Reservation
LotteryScreenUI


LotteryScreenUI
Display active lottery status, countdown timers, and win/loss notifications
Provide date selection and accept/decline controls for lottery winners
Provide owner form to configure lottery dates, winner count, and capacity
Display waitlist roster to owner with broadcast notification compose panel

Lottery
WaitingList
Notification
Reservation


UserProfileScreenUI
Display and edit personal profile information (name, email, phone)
Toggle notification opt-in/opt-out preferences
Display lists of active, past, and cancelled reservations
Provide action to download reservation confirmation CSV
Provide action to delete user profile

User
Reservation


AdminScreenUI
Provide administrative dashboard to browse campsites, profiles, and images
Provide controls to remove violating campsites, profiles, and images
Display searchable logs of all platform notifications
Display searchable logs of all owner-to-entrant messages

Admin
Campsite
User
CampsiteImage
Notification
Message
