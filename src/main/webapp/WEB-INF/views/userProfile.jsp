<!DOCTYPE html>
<html>
  <%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%> <%@ page isELIgnored="false" %> <%@ taglib
  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> <%@ taglib
  prefix="security" uri="http://www.springframework.org/security/tags" %>
  <head>
    <script
      src="https://code.jquery.com/jquery-3.6.3.min.js"
      integrity="sha256-pvPw+upLPUjgMXY0G+8O0xUf+/Im1MZjXxxgOcBQBXU="
      crossorigin="anonymous"
    ></script>
    <link
      rel="stylesheet"
      href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css"
    />
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"
    />
    <meta charset="UTF-8" />
    <title>User Profile</title>
    <style>
      .highlighter {
        display: inline-block;
        background-color: #a8a8a8;
        margin: 15px 0px;
        padding: 5px 10px;
        color: white;
      }

      .link {
        display: block;
        width: 100px;
        font-weight: 500;
      }
    </style>
  </head>
  <body>
    <!-- Navbar -->
    <!-- <div
      class="d-flex justify-content-around align-items-center"
      style="height: 70px; background-color: #a8a8a8"
    >
      <h3>
        <security:authorize access="isAuthenticated()">
          Welcome
          <span id="username"
            ><security:authentication property="principal.username" />
          </span>
          <security:authorize access="hasRole('ADMIN')">
            <button id="question-btn" class="btn">Questions</button>
          </security:authorize>
        </security:authorize>
      </h3>
      <div>
        <a style="color: white; text-decoration: none" href="/home"
          ><button class="btn btn-info">Home</button></a
        >

        <a style="color: white; text-decoration: none" href="/logout"
          ><button class="btn btn-danger">Logout</button></a
        >
      </div>
    </div> -->

    <nav class="navbar navbar-dark bg-primary justify-content-between">
      <a class="navbar-brand" href="/">TravelGig</a>
      <ul class="navbar-nav">
        <li class="nav-item dropdown">
          <a
            class="nav-link dropdown-toggle"
            href="#"
            id="navbarDropdownMenuLink"
            data-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          >
            <security:authorize access="isAuthenticated()">
              Welcome,<span id="username" class="font-weight-bold font-italic ml-1">
              <security:authentication property="principal.username"/>
              </span>
            </security:authorize>
          </a>
          <div class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
            <a class="dropdown-item" href="/home">Home</a>
            <a class="dropdown-item" href="/login?logout">Logout</a>
          </div>
        </li>
      </ul>
    </nav>

    <div class="container mb-4">
      <div class="row justify-content-between align-items-center mt-4">
        <h3 class="">Recent Bookings</h3>
        <div class="btn-group-toggle" data-toggle="buttons">
          <label class="btn btn-outline-success">
            <input type="checkbox" class="filter-checkbox" value="UPCOMING" />
            Upcoming
          </label>
          <label class="btn btn-outline-success">
            <input type="checkbox" class="filter-checkbox" value="COMPLETED" />
            Completed
          </label>
          <label class="btn btn-outline-success">
            <input type="checkbox" class="filter-checkbox" value="CANCELLED" />
            Cancelled
          </label>
        </div>
      </div>
      <div class="bookings-body">
        <div class="d-flex justify-content-center">
          <div id="booking-table" class="mt-3">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>BookingId</th>
                  <th class="d-none">HotelId</th>
                  <th>BookingDate</th>
                  <th>CheckInDate</th>
                  <th>CheckOutDate</th>
                  <th>CustomerMobile</th>
                  <th>Price</th>
                  <th>Status</th>
                  <th style="color: red; text-align: center" colspan="2">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody id="booking-table-body"></tbody>
            </table>
          </div>
        </div>
      </div>
      
      <security:authorize access="isAuthenticated()">
          Welcome
          <span id="username"
            ><security:authentication property="principal.username" />
          </span>
          <security:authorize access="hasAuthority('USER')">
            <h3>Ankit Arora</h3>
          </security:authorize>
        </security:authorize>

      <!-- Old review Modal
      <div class="modal" id="reviewModal">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            
            <div class="modal-header">
              <h4 class="modal-title">Please let Us Know!</h4>
              <button type="button" class="close" data-dismiss="modal">
                &times;
              </button>
            </div>

            <div class="modal-body" id="reviewModal_modalBody">
              <div
                style="overflow: auto; height: 400px"
                class="w-100 d-flex flex-column"
              >
                <div class="h-25">
                  <h4>Question1: How was the Service? (Rate 1 to 5)</h4>
                  <select
                    id="serviceRating"
                    style="height: 40px; width: 100px; font-weight: 500"
                    class="form-control text-center"
                  >
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                  </select>
                </div>
                <div class="h-25">
                  <h4>
                    Question2: Were you satisfied with the amenities? (Rate 1 to
                    5)
                  </h4>
                  <select
                    id="amenitiesRating"
                    style="height: 40px; width: 100px"
                    class="form-control text-center"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option>5</option>
                  </select>
                </div>
                <div class="h-25">
                  <h4>
                    Question3: How comfortable was the booking process? (Rate 1
                    to 5)
                  </h4>
                  <select
                    id="bookingProcessRating"
                    style="height: 40px; width: 100px"
                    class="form-control text-center"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option>5</option>
                  </select>
                </div>
                <div class="h-25">
                  <h4>
                    Question4: How was your whole experience? (Rate 1 to 5)
                  </h4>
                  <select
                    id="wholeExpRating"
                    style="height: 40px; width: 100px"
                    class="form-control text-center"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option>5</option>
                  </select>
                </div>

                <div class="">
                  <h4>Please leave a review (Optional)</h4>
                  <div>
                    <textarea
                      id="review-text"
                      rows="3"
                      style="height: 50px; width: 100%"
                      class="form-control"
                    >
                    Insert text </textarea
                    >
                  </div>
                </div>

                <div>
                  <div>
                    <h3>Overall Ratings:</h3>
                    <span id="review-rating"></span>
                  </div>
                </div>
              </div>
            </div>
            

            
            <div class="modal-footer">
              <div class="w-100 d-flex justify-content-end">
                <button id="review-submit" class="btn btn-primary">
                  Submit
                </button>
              </div>
              <button type="button" class="btn btn-danger" data-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
-->

      <!-- New Review Modal-->
      <div class="modal" id="reviewModal">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
              <h4 class="modal-title">Hotel Review</h4>
              <button type="button" class="close" data-dismiss="modal">
                &times;
              </button>
            </div>

            <!-- Modal body -->
            <div
              class="modal-body"
              id="reviewModal_modalBody"
              style="overflow: auto; height: 400px"
            >
              <div class="container">
                <!-- Question 1 -->
                <div class="row justify-content-between">
                  <p class="mr-2">
                    Question 1: How was the service? (Rate 1 to 5)
                  </p>
                  <select
                    id="serviceRating"
                    class="form-control text-center"
                    style="height: 40px; width: 100px; font-weight: 500"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option selected>5</option>
                  </select>
                </div>

                <!-- Question 2 -->
                <div class="row justify-content-between">
                  <p class="mr-2">
                    Question 2: Were you satisfied with the amenities? (Rate 1
                    to 5)
                  </p>
                  <select
                    id="amenitiesRating"
                    class="form-control text-center"
                    style="height: 40px; width: 100px"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option selected>5</option>
                  </select>
                </div>

                <!-- Question 3 -->
                <div class="row justify-content-between">
                  <p class="mr-2">
                    Question 3: How comfortable was the booking process? (Rate 1
                    to 5)
                  </p>
                  <select
                    id="bookingProcessRating"
                    class="form-control text-center"
                    style="height: 40px; width: 100px"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option selected>5</option>
                  </select>
                </div>

                <!-- Question 4 -->
                <div class="row justify-content-between">
                  <p class="mr-2">
                    Question 4: How was your overall experience? (Rate 1 to 5)
                  </p>
                  <select
                    id="wholeExpRating"
                    class="form-control text-center"
                    style="height: 40px; width: 100px"
                  >
                    <option>1</option>
                    <option>2</option>
                    <option>3</option>
                    <option>4</option>
                    <option selected>5</option>
                  </select>
                </div>

                <!-- Review Text (Optional) -->
                <div class="row justify-content-between">
                  <p class="mr-2">Please leave a review (Optional)</p>
                  <textarea
                    id="review-text"
                    rows="3"
                    class="form-control"
                    placeholder="Insert text"
                  ></textarea>
                </div>
                <hr class="mt-5" />
                <!-- Overall Ratings -->
                <div class="row justify-content-between">
                  <p class="mr-2 font-weight-bold">Overall Ratings:</p>
                  <span id="overallRating"></span>
                </div>
              </div>
            </div>

            <!-- Modal footer -->
            <div class="modal-footer">
              <button id="review-submit" class="btn btn-primary">Submit</button>
              <button type="button" class="btn btn-danger" data-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal" id="question-modal">
        <div class="modal-dialog">
          <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
              <h4 class="modal-title">You have Questions to be Answered!</h4>
              <button type="button" class="close" data-dismiss="modal">
                &times;
              </button>
            </div>

            <div class="modal-body" id="questionModal_body">
              <div class="form-group"></div>
            </div>

            <!-- Modal footer -->
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" data-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal" id="answer-modal">
        <div class="modal-dialog">
          <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
              <h4 class="modal-title">Please Answer!</h4>
              <button type="button" class="close" data-dismiss="modal">
                &times;
              </button>
            </div>

            <div class="modal-body" id="questionModal_body">
              <div id="question"></div>

              <div id="answer">
                <textarea
                  id="answer-text"
                  style="width: 100%; height: 100px"
                ></textarea>
                <div>
                  <button id="submit-answer" class="btn">Submit</button>
                </div>
              </div>
            </div>

            <!-- Modal footer -->
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" data-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal" id="successModal">
        <div class="modal-dialog">
          <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
              <h4 class="modal-title">Confirmed</h4>
              <button type="button" class="close" data-dismiss="modal">
                &times;
              </button>
            </div>

            <div class="modal-body" id="successModel_body">
              <h3>SUCCESS!!!</h3>
            </div>

            <!-- Modal footer -->
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" data-dismiss="modal">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <script src="./js/userProfile.js"></script>
  </body>
</html>
