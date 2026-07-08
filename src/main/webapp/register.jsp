<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Register | E-Volunteer</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="styles.css?v=39">
</head>
<body class="auth-page" data-page="register">
  <main class="auth-layout">
    <section class="auth-intro">
      <a class="brand-link" href="dashboard.jsp"><span class="brand-mark"></span><span>E-Volunteer</span></a>
      <div>
        <h1>Join the community.</h1>
        <p>Create a student account to volunteer or an NGO admin account to organise programs.</p>
      </div>
      <p class="auth-footnote">One account connects your applications, opportunities, and contribution records.</p>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <div class="page-heading compact">
          <h2>Create account</h2>
          <p>All fields are required.</p>
        </div>
        <form id="registerForm" class="form-stack">
          <label class="field">Full name
            <input id="registerName" placeholder="Aina Rahman" required>
          </label>
          <label class="field">Email
            <input id="registerEmail" type="email" placeholder="name@example.com" required>
          </label>
          <label class="field">Role
            <select id="registerRole" required>
              <option value="student">Student</option>
              <option value="admin">NGO Admin</option>
            </select>
          </label>
          <label class="field">Student ID / NGO code
            <input id="registerId" placeholder="2026-001 or NGO-GE01" required>
          </label>
          <label class="field">Password
            <input id="registerPassword" type="password" minlength="5" placeholder="At least 5 characters" required>
          </label>
          <button class="btn primary wide" type="submit">Create account</button>
        </form>
        <p class="auth-switch">Already registered? <a href="login.jsp">Login here</a></p>
      </div>
    </section>
  </main>
  <div class="toast" id="toast" role="status"></div>
  <script src="app.js?v=39"></script>
</body>
</html>



