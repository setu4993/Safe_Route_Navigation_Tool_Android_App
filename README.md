# Safest Route Navigation Tool - Android App

An extension of my project [Fatality Rate Prediction Navigation Tool](https://github.com/setu4993/Fatality_Rate_Prediction_Navigation_Tool) to implement it as an Android app, along with additional considerations of weather and establishing a balance between time of journey and fatality rate.

The app begins with taking inputs of the source and destination locations, and gets the possible routes between them from the Google Maps API. Once the routes are obtained, each route's step-by-step predicted fatality rate is calculated based on the neural network generated in the earlier project. The obtained fatality rates are compared, alongwith the probable weather conditions and time of travel to select the best route that is a balance between the fatality factor and time of travel, and the selected route is displayed to the user on a Google Maps activity.

The selection of the best route is done based on:
- If extreme weather conditions exist on any section of the route, the route is automatically discarded.
- If the differences between the fatality factors are less than 10%, then the fastest route is suggested.
- If the differences are between 10% and 30%, the product of fatality factor and time of journey is compared and the route with the lowest product is chosen.
- If the differences are greater than 10%, the safest route is suggested.
