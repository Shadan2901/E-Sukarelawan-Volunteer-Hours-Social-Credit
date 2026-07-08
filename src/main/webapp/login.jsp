<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Login | E-Volunteer</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="styles.css?v=39">
</head>
<body class="auth-page" data-page="login">
  <main class="auth-layout">
    <section class="auth-intro">
      <a class="brand-link" href="dashboard.jsp"><span class="brand-mark"></span><span>E-Volunteer</span></a>
      <div>
        <h1>Welcome To</h1>
        <h1>E-Volunteer</h1>
        <p>Sign in to manage volunteer opportunities, applications, and verified service hours.</p>
      </div>
      <div class="demo-box">
        <strong>Demo accounts</strong>
        <span>Student: student@demo.my / 12345</span>
        <span>Admin: admin@demo.my / 12345678</span>
      </div>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <div class="page-heading compact">
          <h2>Login</h2>
          <p>Enter your account details.</p>
        </div>
        <form id="loginForm" class="form-stack">
          <label class="field">Role
            <select id="loginRole" required>
              <option value="student">Student</option>
              <option value="admin">NGO Admin</option>
            </select>
          </label>
          <label class="field">Email
            <input id="loginEmail" type="email" placeholder="name@example.com" required>
          </label>
          <label class="field student-id-field">Student ID
            <input id="loginStudentId" type="text" placeholder="Example: A189337" autocomplete="username">
          </label>
          <label class="field">Password
            <input id="loginPassword" type="password" placeholder="Enter password" required>
          </label>
          <button class="btn primary wide" type="submit">Login</button>
        </form>
        <p class="auth-switch">No account? <a href="register.jsp">Register here</a></p>
      </div>
    </section>
  </main>
  <div class="toast" id="toast" role="status"></div>
  <script src="app.js?v=39"></script>
</body>
</html>



