<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Volunteer Hours | E-Volunteer</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="styles.css?v=39">
</head>
<body data-page="hours">
  <div class="app-shell">
    <aside class="sidebar" id="sidebar"></aside>
    <main class="workspace">
      <header class="topbar" id="topbar"></header>
      <section class="page-heading"><h1>Volunteer hours</h1><p id="hoursIntro">Submit completed service and track approval.</p></section>
      <section class="hours-grid">
        <form class="panel student-only" id="hoursForm">
          <div class="panel-head"><div><h2>Submit hours</h2><p>Only admin-approved applications can be used to enter volunteer hours.</p></div></div>
          <div class="panel-body form-stack">
            <label class="field">Activity<select id="activityName" required></select></label>
            <p class="form-note" id="hoursEligibilityNote">Waiting for approved applications.</p>
            <label class="field">Hours contributed<input id="hoursContributed" type="number" min="0.5" max="24" step="0.5" required></label>
            <label class="field">Evidence note<textarea id="hoursNote" placeholder="Supervisor or attendance reference"></textarea></label>
            <button class="btn primary" type="submit">Submit for approval</button>
          </div>
        </form>
        <section class="panel hours-log">
          <div class="panel-head"><div><h2>Hours log</h2><p id="hoursLogCopy">Approved and pending records.</p></div></div>
          <div class="table-wrap">
            <table>
              <thead><tr><th>Student</th><th>Activity</th><th>Hours</th><th>Status</th><th class="admin-only">Action</th></tr></thead>
              <tbody id="hoursTable"></tbody>
            </table>
          </div>
        </section>
      </section>
    </main>
  </div>
  <div class="toast" id="toast" role="status"></div>
  <script src="app.js?v=39"></script>
</body>
</html>



