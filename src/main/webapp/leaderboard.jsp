<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Leaderboard | E-Volunteer</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="styles.css?v=39">
</head>
<body data-page="leaderboard">
  <div class="app-shell">
    <aside class="sidebar" id="sidebar"></aside>
    <main class="workspace">
      <header class="topbar" id="topbar"></header>
      <section class="page-heading"><h1>Volunteer leaderboard</h1><p>Students ranked by approved service hours.</p></section>
      <section class="leader-podium" id="leaderPodium"></section>
      <section class="panel">
        <div class="panel-head"><div><h2>All contributors</h2><p>Only verified hours are counted.</p></div><button class="btn secondary" id="exportBtn" type="button">Export CSV</button></div>
        <div class="table-wrap">
          <table><thead><tr><th>Rank</th><th>Student</th><th>Student ID</th><th>Verified hours</th></tr></thead><tbody id="leaderboardTable"></tbody></table>
        </div>
      </section>
    </main>
  </div>
  <div class="toast" id="toast" role="status"></div>
  <script src="app.js?v=39"></script>
</body>
</html>



