<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Dashboard | E-Volunteer</title>

  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="styles.css?v=39">
</head>

<body data-page="dashboard">
  <div class="app-shell">
    <aside class="sidebar" id="sidebar"></aside>

    <main class="workspace">
      <header class="topbar" id="topbar"></header>

      <!-- HERO DASHBOARD -->
      <section class="dashboard-hero">
        <div class="hero-content">
          <div class="hero-badge">Enterprise Volunteer Management Platform</div>
          <h1>Dashboard</h1>
          <p id="dashboardGreeting">
            Monitor volunteer activities, verified hours, applications, and program performance in one intelligent workspace.
          </p>

          <div class="hero-actions">
            <a href="opportunities.jsp" class="btn-primary">Explore Opportunities</a>
            <a href="hours.jsp" class="btn-secondary">View My Hours</a>
          </div>
        </div>

        <div class="hero-card">
          <div class="hero-card-top">
            <span>System Readiness</span>
            <strong>96%</strong>
          </div>

          <div class="progress-wrap">
            <div class="progress-bar">
              <span style="width: 96%;"></span>
            </div>
          </div>

          <div class="hero-stats">
            <div>
              <strong>24/7</strong>
              <span>Access</span>
            </div>
            <div>
              <strong>Secure</strong>
              <span>Records</span>
            </div>
            <div>
              <strong>Smart</strong>
              <span>Tracking</span>
            </div>
          </div>
        </div>
      </section>

      <!-- METRICS -->
      <section class="metrics advanced-metrics">
        <article class="metric metric-shield">
          <div class="metric-icon">OK</div>
          <div>
            <span>Verified Hours</span>
            <strong id="metricHours">0</strong>
            <small>Approved contributions</small>
          </div>
        </article>

        <article class="metric metric-calendar">
          <div class="metric-icon">EV</div>
          <div>
            <span>Active Events</span>
            <strong id="metricEvents">0</strong>
            <small>You're registered</small>
          </div>
        </article>

        <article class="metric metric-clock">
          <div class="metric-icon">RV</div>
          <div>
            <span>Pending Review</span>
            <strong id="metricPending">0</strong>
            <small>Awaiting approval</small>
          </div>
        </article>

        <article class="metric metric-trophy">
          <div class="metric-icon">#</div>
          <div>
            <span>Student Rank</span>
            <strong id="metricRank">-</strong>
            <small>By verified hours</small>
          </div>
        </article>
      </section>

      <!-- BUSINESS VALUE SECTION -->
      <section class="business-grid">
        <article class="business-card premium-card">
          <div class="card-label">Why companies choose E-Volunteer</div>
          <h2>Centralized volunteer data with real-time monitoring</h2>
          <p>
            E-Volunteer helps organisations manage student volunteers, activity approvals,
            attendance records, reflections, and verified contribution hours through a structured digital platform.
          </p>

          <div class="feature-list">
            <div>
              <strong>01</strong>
              <span>Reduce manual paperwork</span>
            </div>
            <div>
              <strong>02</strong>
              <span>Improve reporting accuracy</span>
            </div>
            <div>
              <strong>03</strong>
              <span>Track volunteer engagement</span>
            </div>
          </div>
        </article>

        <article class="insight-card">
          <h3>Volunteer Performance</h3>
          <p>Monthly activity growth</p>

          <div class="chart-bars">
            <span style="height: 45%;"></span>
            <span style="height: 65%;"></span>
            <span style="height: 50%;"></span>
            <span style="height: 80%;"></span>
            <span style="height: 70%;"></span>
            <span style="height: 92%;"></span>
          </div>

          <div class="growth-indicator">
            <strong>+38%</strong>
            <span>More verified participation this month</span>
          </div>
        </article>
      </section>

      <!-- MAIN GRID -->
      <section class="dashboard-grid">
        <article class="panel modern-panel">
          <div class="panel-head">
            <div>
              <span class="section-tag">Opportunities</span>
              <h2>Upcoming Opportunities</h2>
              <p>Programs that still need volunteers.</p>
            </div>
            <a href="opportunities.jsp">View All</a>
          </div>

          <div class="panel-body activity-list" id="dispatchList"></div>
        </article>

        <article class="panel modern-panel">
          <div class="panel-head">
            <div>
              <span class="section-tag">Applications</span>
              <h2 id="queueTitle">My Applications</h2>
              <p id="queueCopy">Your latest activity.</p>
            </div>
          </div>

          <div class="panel-body activity-list" id="queueList"></div>
        </article>
      </section>

      <!-- CORPORATE TRUST SECTION -->
      <section class="trust-section">
        <article>
          <h3>Audit-ready records</h3>
          <p>Every activity record can be reviewed, verified, and organized for reporting purposes.</p>
        </article>

        <article>
          <h3>Student engagement</h3>
          <p>Students can monitor hours, applications, progress, and upcoming volunteering opportunities.</p>
        </article>

        <article>
          <h3>Management insight</h3>
          <p>Administrators can understand program performance and approval status faster.</p>
        </article>
      </section>

      <!-- ANNOUNCEMENT -->
      <section class="panel announcement-panel advanced-announcement">
        <div class="announcement-icon">!</div>

        <div>
          <h2>Announcement</h2>
          <p>
            Please upload all attendance records and reflections no later than
            3 days after each activity.
          </p>
        </div>

        <time>15 May 2025</time>
      </section>
    </main>
  </div>

  <div class="toast" id="toast" role="status"></div>

  <script src="app.js?v=39"></script>
</body>
</html>



