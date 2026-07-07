const APP_VERSION = 6;
const STORAGE_KEY = "eSukarelawanStateV6";
const SESSION_KEY = "eSukarelawanSessionV1";
const UNIVERSITY_SESSION_KEY = "eSukarelawanUniversityHintV1";
const THEME_KEY = "eSukarelawanThemeV1";
const SIDEBAR_KEY = "eSukarelawanSidebarCollapsedV1";
const WINDOW_STATE_PREFIX = "ESUKARELAWAN:";
const pageName = document.body.dataset.page || "entry";
const API_BASE = `${window.location.origin}${contextPath()}/resources/api`;

const seedState = {
  version: APP_VERSION,
  users: [
    {
      id: 1,
      fullName: "UKM Volunteer Coordinator",
      email: "admin@demo.my",
      passwordHash: "ef797c8118f02dfb649607dd5d3f8c7623048c9c063d532cc95c5ed7a898a64f",
      role: "admin",
      referenceId: "UKM-ADMIN",
      ngoName: "Universiti Kebangsaan Malaysia"
    },
    {
      id: 2,
      fullName: "Aisyah Maisarah",
      email: "student@demo.my",
      passwordHash: "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5",
      role: "student",
      referenceId: "A189337"
    }
  ],
  opportunities: [
    { id: 1, adminId: 1, event: "Program Pembersihan Sungai Langat", ngo: "UKM Sukarelawan", description: "Program komuniti membersihkan kawasan sungai bersama penduduk setempat.", date: "2025-05-24", seats: 28, location: "Kajang, Selangor", status: "open", category: "Environment" },
    { id: 2, adminId: 1, event: "Kelas Tuisyen Komuniti", ngo: "UKM Bakti Siswa", description: "Bantu pelajar sekolah rendah melalui kelas bimbingan hujung minggu.", date: "2025-05-31", seats: 16, location: "UKM, Bangi", status: "open", category: "Education" },
    { id: 3, adminId: 1, event: "Sahabat Warga: Lawatan & Sumbangan", ngo: "Kelab Kebajikan UKM", description: "Lawatan sokongan sosial dan penyerahan sumbangan ke pusat jagaan.", date: "2025-06-07", seats: 10, location: "Pusat Jagaan Kasih Harmoni, Kajang", status: "limited", category: "Community" },
    { id: 4, adminId: 1, event: "Dapur Komuniti Ramadan", ngo: "Sukarelawan Mahasiswa", description: "Menyediakan dan mengagihkan makanan kepada keluarga memerlukan.", date: "2025-06-14", seats: 0, location: "Bangi", status: "closed", category: "Food Aid" }
  ],
  applications: [
    { id: 1, studentId: 2, opportunityId: 1, applicationDate: "2025-05-18", status: "approved" },
    { id: 2, studentId: 2, opportunityId: 2, applicationDate: "2025-05-20", status: "approved" },
    { id: 3, studentId: 2, opportunityId: 3, applicationDate: "2025-05-21", status: "pending" }
  ],
  hours: [
    { id: 1, studentId: 2, opportunityId: 1, activity: "Program Pembersihan Sungai Langat", amount: 84.5, status: "approved", note: "Attendance verified by programme coordinator", approvedByAdminId: 1 },
    { id: 2, studentId: 2, opportunityId: 2, activity: "Kelas Tuisyen Komuniti", amount: 36, status: "approved", note: "Teaching log completed", approvedByAdminId: 1 },
    { id: 3, studentId: 2, opportunityId: 3, activity: "Sahabat Warga: Lawatan & Sumbangan", amount: 2, status: "pending", note: "Reflection pending review", approvedByAdminId: null },
    { id: 4, studentName: "Muhammad Danish", opportunityId: 1, activity: "Program Pembersihan Sungai Langat", amount: 6, status: "pending", note: "18 May 2025", approvedByAdminId: null },
    { id: 5, studentName: "Nur Adlina", opportunityId: 2, activity: "Kelas Tuisyen Komuniti", amount: 4, status: "pending", note: "11 May 2025", approvedByAdminId: null },
    { id: 6, studentName: "Arif Hakimi", opportunityId: 3, activity: "Sahabat Warga: Lawatan & Sumbangan", amount: 5.5, status: "pending", note: "10 May 2025", approvedByAdminId: null },
    { id: 7, studentName: "Farah Nazihah", opportunityId: 1, activity: "Program Pembersihan Sungai Langat", amount: 6, status: "approved", note: "4 May 2025", approvedByAdminId: 1 },
    { id: 8, studentName: "Haqim Rashid", opportunityId: 2, activity: "Kelas Tuisyen Komuniti", amount: 3, status: "approved", note: "3 May 2025", approvedByAdminId: 1 }
  ],
  feedback: [
    {
      id: 1,
      studentId: 2,
      subject: "Certificate download",
      message: "Can I get my verified volunteer hours certificate after all hours are approved?",
      status: "replied",
      createdAt: "2025-05-22T10:30:00",
      adminId: 1,
      adminReply: "Yes. Once your records are approved, open the leaderboard/export area and use it for reporting.",
      repliedAt: "2025-05-22T14:05:00"
    }
  ],
  sessionUserId: null
};

let state = clone(seedState);
let activeFilter = "all";
let editingOpportunityId = null;
let activeQueueTab = "hours";
let toastTimer = null;
let dataSource = null;

function contextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (!parts.length || window.location.protocol === "file:") return "";
  return parts[0].includes(".html") ? "" : `/${parts[0]}`;
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function validState(value) {
  return Boolean(value?.version === APP_VERSION && value?.users && value?.opportunities && value?.applications && value?.hours && value?.feedback);
}

function nextId(collection) {
  return collection.reduce((maximum, item) => Math.max(maximum, Number(item.id) || 0), 0) + 1;
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, character => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;"
  })[character]);
}

function initials(name) {
  return String(name).split(" ").filter(Boolean).map(part => part[0]).join("").slice(0, 2).toUpperCase();
}

function formatDate(dateString) {
  return new Intl.DateTimeFormat("en-MY", { day: "2-digit", month: "short", year: "numeric" })
    .format(new Date(`${dateString}T00:00:00`));
}

function formatDateTime(value) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("en-MY", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

function formatHours(value) {
  return Number(value || 0).toLocaleString("en-MY", { maximumFractionDigits: 1 });
}

function categoryClass(category = "") {
  return String(category).toLowerCase().replace(/[^a-z0-9]+/g, "-") || "community";
}

const universityRules = [
  {
    code: "UKM",
    name: "Universiti Kebangsaan Malaysia",
    motto: "ILMU, MUTU DAN BUDI",
    tests: [/^A\d{5,}$/i, /^UKM/i, /UKM/i, /Kebangsaan/i]
  },
  {
    code: "UiTM",
    name: "Universiti Teknologi MARA",
    motto: "USAHA, TAQWA, MULIA",
    tests: [/^UITM/i, /^20\d{5,}$/i, /Teknologi MARA/i, /MARA/i]
  },
  {
    code: "UM",
    name: "Universiti Malaya",
    motto: "ILMU PUNCA KEMAJUAN",
    tests: [/^UM/i, /Malaya/i]
  },
  {
    code: "UPM",
    name: "Universiti Putra Malaysia",
    motto: "BERILMU BERBAKTI",
    tests: [/^UPM/i, /Putra/i]
  }
];

function detectUniversityFromId(value = "") {
  const input = String(value).trim();
  if (!input) return null;
  return universityRules.find(item => item.tests.some(rule => rule.test(input))) || {
    code: "UNI",
    name: "University not detected",
    motto: "Check the student ID prefix"
  };
}

function universityForUser(user) {
  const storedHint = sessionStorage.getItem(UNIVERSITY_SESSION_KEY);
  const detected = detectUniversityFromId(user?.role === "student" ? storedHint || user.referenceId : user?.ngoName || user?.referenceId);
  if (detected && detected.code !== "UNI") return detected;
  return {
    code: "ES",
    name: "E-Sukarelawan",
    motto: "Volunteer management platform"
  };
}

function profileForUser(user) {
  if (!user.profile) user.profile = {};
  return user.profile;
}

function avatarMarkup(user, className = "avatar") {
  const profile = user?.profile || {};
  if (profile.photo) {
    return `<div class="${className} photo-avatar"><img src="${escapeHtml(profile.photo)}" alt="${escapeHtml(user.fullName)} profile picture"></div>`;
  }
  return `<div class="${className}">${initials(user?.fullName || "User")}</div>`;
}

function opportunityTime(item) {
  const slots = {
    Education: "2:00 PM",
    Community: "9:00 AM",
    Environment: "",
    "Food Aid": "10:00 AM"
  };
  return slots[item.category] ? `${formatDate(item.date)} · ${slots[item.category]}` : formatDate(item.date);
}

function userName(studentId, fallback = "Volunteer") {
  return state.users.find(user => user.id === studentId)?.fullName || fallback;
}

function displayNameForUser(user) {
  if (!user) return "User";
  if (user.role !== "admin") return user.fullName || "User";
  const cleaned = String(user.fullName || "")
    .replace(/\b(UKM|UiTM|UM|UPM)\b/gi, "")
    .replace(/\bUniversiti\s+(Kebangsaan|Teknologi MARA|Malaya|Putra)\s+Malaysia\b/gi, "")
    .replace(/\s+/g, " ")
    .trim();
  return cleaned || "Admin Coordinator";
}

async function hashPassword(password) {
  if (!globalThis.crypto?.subtle) return `plain:${password}`;
  const input = new TextEncoder().encode(password);
  const digest = await crypto.subtle.digest("SHA-256", input);
  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, "0")).join("");
}

async function passwordMatches(user, password) {
  const hashed = await hashPassword(password);
  if (user.passwordHash === hashed) return true;
  if (user.passwordHash?.startsWith("plain:")) return user.passwordHash === `plain:${password}`;
  return (user.email === "admin@demo.my" && password === "12345678")
    || (user.email === "student@demo.my" && password === "12345");
}

function showToast(message) {
  const toast = document.querySelector("#toast");
  if (!toast) return;
  clearTimeout(toastTimer);
  toast.textContent = message;
  toast.classList.add("show");
  toastTimer = setTimeout(() => toast.classList.remove("show"), 2700);
}

function go(file) {
  dataSource?.save?.();
  window.location.href = file;
}

function currentTheme() {
  try {
    return localStorage.getItem(THEME_KEY) === "dark" ? "dark" : "light";
  } catch {
    return "light";
  }
}

function applyTheme(theme = currentTheme()) {
  const selected = theme === "dark" ? "dark" : "light";
  document.documentElement.dataset.theme = selected;
  document.body.dataset.theme = selected;

  const toggle = document.querySelector("#themeToggleBtn");
  if (!toggle) return;
  const nextTheme = selected === "dark" ? "light" : "dark";
  toggle.setAttribute("aria-label", `Switch to ${nextTheme} theme`);
  toggle.setAttribute("aria-pressed", String(selected === "dark"));
  toggle.title = `Switch to ${nextTheme} theme`;
  toggle.querySelector(".theme-glyph").textContent = selected === "dark" ? "L" : "D";
}

function toggleTheme() {
  const nextTheme = currentTheme() === "dark" ? "light" : "dark";
  try {
    localStorage.setItem(THEME_KEY, nextTheme);
  } catch {
    // Theme still changes for this page even when storage is unavailable.
  }
  applyTheme(nextTheme);
  showToast(`${nextTheme === "dark" ? "Dark" : "Light"} theme enabled.`);
}

function sidebarCollapsed() {
  try {
    return localStorage.getItem(SIDEBAR_KEY) === "true";
  } catch {
    return false;
  }
}

function applySidebarState(collapsed = sidebarCollapsed()) {
  document.body.classList.toggle("sidebar-collapsed", collapsed);
  const toggle = document.querySelector("#sidebarToggleBtn");
  if (!toggle) return;
  toggle.textContent = collapsed ? ">" : "<";
  toggle.setAttribute("aria-label", collapsed ? "Show sidebar" : "Hide sidebar");
  toggle.setAttribute("aria-expanded", String(!collapsed));
  toggle.title = collapsed ? "Show sidebar" : "Hide sidebar";
}

function setupSidebarToggle() {
  if (!document.querySelector("#sidebar") || !document.querySelector("#topbar")) return;
  let toggle = document.querySelector("#sidebarToggleBtn");
  if (!toggle) {
    toggle = document.createElement("button");
    toggle.id = "sidebarToggleBtn";
    toggle.className = "sidebar-toggle";
    toggle.type = "button";
    document.body.appendChild(toggle);
  }
  toggle.onclick = () => {
    const next = !document.body.classList.contains("sidebar-collapsed");
    try {
      localStorage.setItem(SIDEBAR_KEY, String(next));
    } catch {
      // The sidebar still collapses for this page when storage is unavailable.
    }
    applySidebarState(next);
  };
  applySidebarState();
}

class LocalDataSource {
  constructor() {
    this.mode = "local";
  }

  async init() {
    state = this.load();
  }

  load() {
    try {
      if (window.name.startsWith(WINDOW_STATE_PREFIX)) {
        const fromTab = JSON.parse(window.name.slice(WINDOW_STATE_PREFIX.length));
        if (validState(fromTab)) return fromTab;
      }
      const fromStorage = JSON.parse(localStorage.getItem(STORAGE_KEY));
      if (validState(fromStorage)) return fromStorage;
    } catch (error) {
      console.warn("Saved data could not be loaded.", error);
    }
    return clone(seedState);
  }

  save() {
    const serialized = JSON.stringify(state);
    window.name = `${WINDOW_STATE_PREFIX}${serialized}`;
    try {
      localStorage.setItem(STORAGE_KEY, serialized);
    } catch (error) {
      console.warn("Local persistence is unavailable.", error);
    }
  }

  async login({ role, email, password }) {
    const user = state.users.find(item => item.email.toLowerCase() === email.toLowerCase() && item.role === role);
    if (!user || !(await passwordMatches(user, password))) throw new Error("Email, password, or role is incorrect.");
    state.sessionUserId = user.id;
    this.save();
    return user;
  }

  async register(payload) {
    if (state.users.some(user => user.email.toLowerCase() === payload.email.toLowerCase())) {
      throw new Error("An account with this email already exists.");
    }
    const user = {
      id: nextId(state.users),
      fullName: payload.fullName,
      email: payload.email,
      passwordHash: await hashPassword(payload.password),
      role: payload.role,
      referenceId: payload.referenceId,
      ...(payload.role === "admin" ? { ngoName: payload.fullName } : {})
    };
    state.users.push(user);
    state.sessionUserId = user.id;
    this.save();
    return user;
  }

  async logout() {
    state.sessionUserId = null;
    this.save();
  }

  async createOpportunity(payload) {
    state.opportunities.unshift({ id: nextId(state.opportunities), ...payload });
    this.save();
  }

  async updateOpportunity(id, payload) {
    const opportunity = state.opportunities.find(item => item.id === id);
    Object.assign(opportunity, payload);
    this.save();
  }

  async deleteOpportunity(id) {
    state.opportunities = state.opportunities.filter(item => item.id !== id);
    state.applications = state.applications.filter(item => item.opportunityId !== id);
    this.save();
  }

  async apply(opportunityId, studentId) {
    const opportunity = state.opportunities.find(item => item.id === opportunityId);
    state.applications.push({
      id: nextId(state.applications),
      studentId,
      opportunityId,
      applicationDate: new Date().toISOString().slice(0, 10),
      status: "pending"
    });
    opportunity.seats = Math.max(0, opportunity.seats - 1);
    if (opportunity.seats === 0) opportunity.status = "closed";
    this.save();
  }

  async submitHours(payload) {
    state.hours.unshift({ id: nextId(state.hours), status: "pending", approvedByAdminId: null, ...payload });
    this.save();
  }

  async reviewHours(id, action, adminId) {
    const record = state.hours.find(item => item.id === id);
    record.status = action === "approve" ? "approved" : "rejected";
    record.approvedByAdminId = adminId;
    this.save();
  }

  async reviewApplication(id, action, adminId) {
    const application = state.applications.find(item => item.id === id);
    if (!application) throw new Error("Application not found.");
    application.status = action === "approve" ? "approved" : "rejected";
    application.reviewedByAdminId = adminId;
    application.reviewedDate = new Date().toISOString().slice(0, 10);
    this.save();
  }

  async updateProfile(id, payload) {
    const user = state.users.find(item => item.id === id);
    if (!user) throw new Error("User not found.");
    Object.assign(user, payload);
    user.profile = payload.profile || {};
    if (user.role === "admin") user.ngoName = payload.fullName;
    this.save();
    return user;
  }

  async submitFeedback(payload) {
    const feedback = {
      id: nextId(state.feedback),
      studentId: payload.studentId,
      subject: payload.subject,
      message: payload.message,
      status: "open",
      createdAt: new Date().toISOString(),
      adminId: null,
      adminReply: null,
      repliedAt: null
    };
    state.feedback.unshift(feedback);
    this.save();
    return feedback;
  }

  async replyFeedback(id, payload) {
    const feedback = state.feedback.find(item => item.id === id);
    if (!feedback) throw new Error("Feedback not found.");
    feedback.status = "replied";
    feedback.adminId = payload.adminId;
    feedback.adminReply = payload.reply;
    feedback.repliedAt = new Date().toISOString();
    this.save();
    return feedback;
  }
}

class ApiDataSource extends LocalDataSource {
  constructor() {
    super();
    this.mode = "api";
  }

  async init() {
    const sessionUserId = Number(sessionStorage.getItem(SESSION_KEY)) || null;
    const serverState = await apiFetch("/state");
    state = { ...serverState, sessionUserId };
  }

  save() {
    if (state.sessionUserId) sessionStorage.setItem(SESSION_KEY, String(state.sessionUserId));
    else sessionStorage.removeItem(SESSION_KEY);
  }

  async refresh() {
    const serverState = await apiFetch("/state");
    state = { ...serverState, sessionUserId: state.sessionUserId };
    this.save();
  }

  async login(payload) {
    const user = await apiFetch("/auth/login", { method: "POST", body: payload });
    state.sessionUserId = user.id;
    await this.refresh();
    state.sessionUserId = user.id;
    this.save();
    return user;
  }

  async register(payload) {
    const user = await apiFetch("/auth/register", { method: "POST", body: payload });
    state.sessionUserId = user.id;
    await this.refresh();
    state.sessionUserId = user.id;
    this.save();
    return user;
  }

  async logout() {
    state.sessionUserId = null;
    this.save();
  }

  async createOpportunity(payload) {
    await apiFetch("/opportunities", { method: "POST", body: payload });
    await this.refresh();
  }

  async updateOpportunity(id, payload) {
    await apiFetch(`/opportunities/${id}`, { method: "PUT", body: payload });
    await this.refresh();
  }

  async deleteOpportunity(id) {
    await apiFetch(`/opportunities/${id}`, { method: "DELETE" });
    await this.refresh();
  }

  async apply(opportunityId, studentId) {
    await apiFetch("/applications", { method: "POST", body: { opportunityId, studentId } });
    await this.refresh();
  }

  async submitHours(payload) {
    await apiFetch("/hours", { method: "POST", body: payload });
    await this.refresh();
  }

  async reviewHours(id, action, adminId) {
    await apiFetch(`/hours/${id}/${action}`, { method: "POST", body: { adminId } });
    await this.refresh();
  }

  async reviewApplication(id, action, adminId) {
    await apiFetch(`/applications/${id}/${action}`, { method: "POST", body: { adminId } });
    await this.refresh();
  }

  async updateProfile(id, payload) {
    const user = await apiFetch(`/users/${id}/profile`, { method: "PUT", body: payload });
    await this.refresh();
    return user;
  }

  async submitFeedback(payload) {
    await apiFetch("/feedback", { method: "POST", body: payload });
    await this.refresh();
  }

  async replyFeedback(id, payload) {
    await apiFetch(`/feedback/${id}/reply`, { method: "POST", body: payload });
    await this.refresh();
  }
}

async function apiFetch(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method: options.method || "GET",
      headers: { "Content-Type": "application/json", "Accept": "application/json" },
      body: options.body ? JSON.stringify(options.body) : undefined
    });
  } catch {
    throw new Error("Could not reach the server. Try a smaller picture or restart the Java server.");
  }
  const text = await response.text();
  if (!response.ok) {
    let message = "The server could not complete the request.";
    try {
      const error = JSON.parse(text);
      message = error.message || message;
    } catch {
      message = text || message;
    }
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return text ? JSON.parse(text) : null;
}

async function chooseDataSource() {
  if (window.location.protocol !== "file:") {
    const api = new ApiDataSource();
    try {
      await api.init();
      return api;
    } catch (error) {
      console.info("Backend API unavailable. Using local demo data.", error);
    }
  }
  const local = new LocalDataSource();
  await local.init();
  return local;
}

function currentUser() {
  return state.users.find(user => user.id === state.sessionUserId) || null;
}

function protectPage() {
  const protectedPages = ["dashboard", "opportunities", "hours", "leaderboard", "profile"];
  if (protectedPages.includes(pageName) && !currentUser()) {
    go("login.html");
    return false;
  }
  return true;
}

function setRoleClasses() {
  const user = currentUser();
  if (user) document.body.dataset.role = user.role;
  document.body.dataset.source = dataSource?.mode || "local";
}

function renderShell() {
  const sidebar = document.querySelector("#sidebar");
  const topbar = document.querySelector("#topbar");
  const user = currentUser();
  if (!sidebar || !topbar || !user) return;
  const university = universityForUser(user);
  const isStudent = user.role === "student";
  const displayName = displayNameForUser(user);

  const links = [
    ["dashboard", "dashboard.html", "🏠", "Dashboard"],
    ["opportunities", "opportunities.html", "🤝", "Opportunities"],
    ["hours", "hours.html", "⏱️", "Volunteer hours"],
    ["leaderboard", "leaderboard.html", "🏆", "Leaderboard"]
  ];

  sidebar.innerHTML = `
    <div class="side-brand">
      <span class="brand-mark"></span>
      <div><strong>E-Sukarelawan</strong><span>Hero Kebersihan</span></div>
    </div>
    <nav class="side-nav">
      ${links.map(([page, href, icon, label]) => `
        <a class="nav-link ${pageName === page ? "active" : ""}" href="${href}">
          <span class="nav-icon">${icon}</span><span>${label}</span>
        </a>
      `).join("")}
    </nav>
    <div class="side-user">
      <strong>${escapeHtml(displayName)}</strong>
      <span>${user.role === "admin" ? "NGO Admin" : escapeHtml(user.referenceId)}</span>
    </div>
  `;

  topbar.innerHTML = `
    <div class="system-state">${dataSource.mode === "api" ? "Server connected" : "Demo mode"}</div>
    <div class="top-profile">
      <div class="avatar">${initials(user.fullName)}</div>
      <div class="profile-copy"><strong>${escapeHtml(user.fullName)}</strong><span>${escapeHtml(user.email)}</span></div>
    </div>
    <button class="btn secondary" type="button" id="logoutBtn">Logout</button>
  `;
  document.querySelector("#logoutBtn").addEventListener("click", async () => {
    await dataSource.logout();
    go("login.html");
  });
  setupSidebarToggle();
}

function renderShell() {
  const sidebar = document.querySelector("#sidebar");
  const topbar = document.querySelector("#topbar");
  const user = currentUser();
  if (!sidebar || !topbar || !user) return;
  const university = universityForUser(user);
  const isStudent = user.role === "student";

  const links = [
    ["dashboard", "dashboard.html", "🏠", "Dashboard"],
    ["opportunities", "opportunities.html", "🤝", "Volunteer opportunities"],
    ["hours", "hours.html", "⏱️", "Volunteer hours"],
    ["leaderboard", "leaderboard.html", "🏆", "Leaderboard"],
    ["feedback", "feedback.html", "💬", "Feedback"],
    ["profile", "profile.html", "👤", "Profile"]
  ];

  sidebar.innerHTML = `
    <div class="side-brand">
      <span class="brand-mark"></span>
      <div><strong>E-Sukarelawan</strong><span>Sukarelawan, Masyarakat Berdaya</span></div>
    </div>
    <nav class="side-nav">
      ${links.map(([page, href, icon, label]) => `
        <a class="nav-link ${pageName === page ? "active" : ""}" href="${href}">
          <span class="nav-icon">${icon}</span><span>${label}</span>
        </a>
      `).join("")}
    </nav>
    <div class="side-user">
      <strong>${escapeHtml(user.fullName)}</strong>
      <span>${user.role === "admin" ? "NGO Admin" : escapeHtml(user.referenceId)}</span>
    </div>
    ${isStudent ? `
      <div class="university-mark">
        <span class="crest university-logo university-logo-${categoryClass(university.code)}" data-logo="${escapeHtml(university.code)}"></span>
        <div><strong>${escapeHtml(university.name)}</strong><span>${escapeHtml(university.motto)}</span></div>
      </div>
    ` : ""}
  `;

  topbar.innerHTML = `
    <div class="system-state">${dataSource.mode === "api" ? "Server connected" : "Demo mode"}</div>
    <button class="bell-btn theme-toggle" type="button" id="themeToggleBtn" aria-label="Switch to dark theme" aria-pressed="false">
      <span class="theme-glyph">D</span><span class="theme-dot"></span>
    </button>
    <a class="top-profile" href="profile.html" aria-label="Open profile page">
      ${avatarMarkup({ ...user, fullName: displayName })}
      <div class="profile-copy"><strong>${escapeHtml(displayName)}</strong><span>${isStudent ? `Student - ${escapeHtml(university.code)}` : "Coordinator"}</span></div>
    </a>
    <button class="btn secondary logout-btn" type="button" id="logoutBtn">Logout</button>
  `;
  applyTheme();
  document.querySelector("#themeToggleBtn")?.addEventListener("click", toggleTheme);
  document.querySelector("#logoutBtn").addEventListener("click", async () => {
    await dataSource.logout();
    go("login.html");
  });
  setupSidebarToggle();
}

function renderDashboard() {
  const user = currentUser();
  if (!user) return;

  document.querySelector("#dashboardGreeting").textContent = user.role === "admin"
    ? "Monitor programmes, participation and submitted service hours."
    : "Track your applications, approved hours and current contribution rank.";

  const relevantHours = user.role === "student"
    ? state.hours.filter(item => item.studentId === user.id)
    : state.hours;
  const verifiedHours = relevantHours.filter(item => item.status === "approved")
    .reduce((sum, item) => sum + Number(item.amount), 0);
  const pendingHours = relevantHours.filter(item => item.status === "pending").length;
  const activeEvents = state.opportunities.filter(item => item.status !== "closed").length;
  const rankings = studentRankings();
  const rank = user.role === "student" ? rankings.findIndex(item => item.id === user.id) + 1 : 0;

  document.querySelector("#metricHours").textContent = verifiedHours;
  document.querySelector("#metricEvents").textContent = activeEvents;
  document.querySelector("#metricPending").textContent = pendingHours;
  document.querySelector("#metricRank").textContent = rank ? `#${rank}` : "—";

  document.querySelector("#dispatchList").innerHTML = state.opportunities
    .filter(item => item.status !== "closed")
    .slice(0, 4)
    .map(item => `
      <div class="activity-row">
        <div><strong>${escapeHtml(item.event)}</strong><span>${escapeHtml(item.location)} · ${formatDate(item.date)}</span></div>
        <span class="tag ${item.status}">${escapeHtml(item.status)}</span>
      </div>
    `).join("") || `<div class="empty">No active opportunities.</div>`;

  const queueTitle = document.querySelector("#queueTitle");
  const queueCopy = document.querySelector("#queueCopy");
  const queueList = document.querySelector("#queueList");

  if (user.role === "admin") {
    queueTitle.textContent = "Approval queue";
    queueCopy.textContent = "Volunteer hours waiting for review.";
    const pending = state.hours.filter(item => item.status === "pending");
    queueList.innerHTML = pending.map(item => `
      <div class="activity-row">
        <div><strong>${escapeHtml(userName(item.studentId, item.studentName))}</strong><span>${escapeHtml(item.activity)} · ${item.amount} hours</span></div>
        <span class="tag pending">Pending</span>
      </div>
    `).join("") || `<div class="empty">Nothing needs review.</div>`;
  } else {
    queueTitle.textContent = "My applications";
    queueCopy.textContent = "Your latest volunteer applications.";
    const applications = state.applications.filter(item => item.studentId === user.id);
    queueList.innerHTML = applications.map(application => {
      const opportunity = state.opportunities.find(item => item.id === application.opportunityId);
      if (!opportunity) return "";
      return `
        <div class="activity-row">
          <div><strong>${escapeHtml(opportunity.event)}</strong><span>${escapeHtml(opportunity.ngo)} · ${formatDate(opportunity.date)}</span></div>
          <span class="tag ${application.status}">${escapeHtml(application.status)}</span>
        </div>
      `;
    }).join("") || `<div class="empty">No applications yet.</div>`;
  }
}

function filteredOpportunities() {
  const search = document.querySelector("#globalSearch");
  const query = search ? search.value.trim().toLowerCase() : "";
  return state.opportunities.filter(item => {
    const matchesFilter = activeFilter === "all" || item.status === activeFilter;
    const matchesQuery = !query || [item.event, item.ngo, item.location, item.category, item.description]
      .some(value => String(value).toLowerCase().includes(query));
    return matchesFilter && matchesQuery;
  });
}

function renderOpportunities() {
  const user = currentUser();
  const list = document.querySelector("#opportunityList");
  const items = filteredOpportunities();
  if (!user || !list) return;

  document.querySelector("#opportunityIntro").textContent = user.role === "admin"
    ? "Create and manage programmes for student volunteers."
    : "Browse programmes and apply to volunteer.";
  document.querySelector("#resultCount").textContent = `${items.length} ${items.length === 1 ? "result" : "results"}`;

  list.innerHTML = items.length ? items.map(item => {
    const application = state.applications.find(entry =>
      entry.studentId === user.id && entry.opportunityId === item.id
    );
    const action = user.role === "admin"
      ? `<button class="btn blue" data-action="edit" data-id="${item.id}" type="button">Edit</button>
         <button class="btn danger" data-action="delete" data-id="${item.id}" type="button">Delete</button>`
      : application
        ? `<button class="btn secondary" type="button" disabled>${escapeHtml(application.status)}</button>`
        : `<button class="btn primary" data-action="apply" data-id="${item.id}" type="button" ${item.status === "closed" ? "disabled" : ""}>Apply</button>`;
    return `
      <article class="opportunity-row">
        <div><h3>${escapeHtml(item.event)}</h3><p>${escapeHtml(item.category)} · ${escapeHtml(item.location)}</p><p>${escapeHtml(item.description)}</p></div>
        <div class="opportunity-meta"><strong>${escapeHtml(item.ngo)}</strong><br>${formatDate(item.date)}</div>
        <div><span class="tag ${item.status}">${escapeHtml(item.status)}</span><p>${item.seats} seats</p></div>
        <div class="row-actions">${action}</div>
      </article>
    `;
  }).join("") : `<div class="empty">No opportunities match your search.</div>`;
}

function resetOpportunityForm() {
  editingOpportunityId = null;
  const form = document.querySelector("#opportunityForm");
  if (!form) return;
  form.reset();
  form.classList.add("hidden");
  document.querySelector("#opportunityFormTitle").textContent = "New opportunity";
  document.querySelector("#saveOpportunityBtn").textContent = "Create";
}

function opportunityPayload(form, user) {
  const data = new FormData(form);
  return {
    adminId: user.id,
    event: data.get("eventName").trim(),
    ngo: data.get("ngoName").trim(),
    description: `${data.get("eventCategory")} volunteer programme.`,
    date: data.get("eventDate"),
    seats: Number(data.get("eventSeats")),
    location: data.get("eventLocation").trim(),
    status: data.get("eventStatus"),
    category: data.get("eventCategory")
  };
}

function setupOpportunityPage() {
  const user = currentUser();
  const form = document.querySelector("#opportunityForm");
  const list = document.querySelector("#opportunityList");
  if (!user || !form || !list) return;

  document.querySelector("#showOpportunityForm")?.addEventListener("click", () => {
    form.classList.remove("hidden");
    form.scrollIntoView({ behavior: "smooth", block: "start" });
  });
  document.querySelector("#cancelEditBtn")?.addEventListener("click", resetOpportunityForm);
  document.querySelector("#globalSearch")?.addEventListener("input", renderOpportunities);
  document.querySelectorAll(".filter-btn").forEach(button => {
    button.addEventListener("click", () => {
      activeFilter = button.dataset.filter;
      document.querySelectorAll(".filter-btn").forEach(item => item.classList.toggle("active", item === button));
      renderOpportunities();
    });
  });

  form.addEventListener("submit", async event => {
    event.preventDefault();
    if (user.role !== "admin") return;
    const payload = opportunityPayload(form, user);
    try {
      if (editingOpportunityId) {
        await dataSource.updateOpportunity(editingOpportunityId, payload);
        showToast("Opportunity updated.");
      } else {
        await dataSource.createOpportunity(payload);
        showToast("Opportunity created.");
      }
      resetOpportunityForm();
      renderOpportunities();
    } catch (error) {
      showToast(error.message);
    }
  });

  list.addEventListener("click", async event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const opportunity = state.opportunities.find(item => item.id === Number(button.dataset.id));
    if (!opportunity) return;

    if (button.dataset.action === "apply" && user.role === "student") {
      try {
        await dataSource.apply(opportunity.id, user.id);
        renderOpportunities();
        showToast("Application submitted.");
      } catch (error) {
        showToast(error.message);
      }
    }

    if (button.dataset.action === "edit" && user.role === "admin") {
      editingOpportunityId = opportunity.id;
      form.eventName.value = opportunity.event;
      form.ngoName.value = opportunity.ngo;
      form.eventDate.value = opportunity.date;
      form.eventSeats.value = opportunity.seats;
      form.eventStatus.value = opportunity.status;
      form.eventLocation.value = opportunity.location;
      form.eventCategory.value = opportunity.category;
      form.classList.remove("hidden");
      document.querySelector("#opportunityFormTitle").textContent = "Edit opportunity";
      document.querySelector("#saveOpportunityBtn").textContent = "Save changes";
      form.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    if (button.dataset.action === "delete" && user.role === "admin") {
      if (!window.confirm(`Delete "${opportunity.event}"?`)) return;
      try {
        await dataSource.deleteOpportunity(opportunity.id);
        renderOpportunities();
        showToast("Opportunity deleted.");
      } catch (error) {
        showToast(error.message);
      }
    }
  });
}

function renderActivityOptions() {
  const user = currentUser();
  const select = document.querySelector("#activityName");
  if (!user || !select) return;
  const opportunities = state.applications
    .filter(item => item.studentId === user.id && item.status !== "rejected")
    .map(application => state.opportunities.find(item => item.id === application.opportunityId))
    .filter(Boolean);
  select.innerHTML = opportunities.length
    ? `<option value="">Select activity</option>${opportunities.map(item => `<option value="${item.id}">${escapeHtml(item.event)}</option>`).join("")}`
    : `<option value="">Apply for an opportunity first</option>`;
}

function renderHours() {
  const user = currentUser();
  const table = document.querySelector("#hoursTable");
  if (!user || !table) return;
  const visibleHours = user.role === "student"
    ? state.hours.filter(item => item.studentId === user.id)
    : state.hours;
  document.querySelector("#hoursIntro").textContent = user.role === "admin"
    ? "Review submitted service hours and keep records verified."
    : "Submit completed service and track approval.";
  document.querySelector("#hoursLogCopy").textContent = user.role === "admin"
    ? "All student submissions."
    : "Your approved and pending records.";

  table.innerHTML = visibleHours.length ? visibleHours.map(item => {
    const action = user.role === "admin" && item.status === "pending"
      ? `<div class="row-actions"><button class="btn blue" data-hours-action="approve" data-id="${item.id}" type="button">Approve</button><button class="btn danger" data-hours-action="reject" data-id="${item.id}" type="button">Reject</button></div>`
      : user.role === "admin" ? "Reviewed" : "";
    return `
      <tr>
        <td>${escapeHtml(userName(item.studentId, item.studentName))}</td>
        <td>${escapeHtml(item.activity)}<div class="cell-note">${escapeHtml(item.note)}</div></td>
        <td>${item.amount}</td>
        <td><span class="tag ${item.status}">${escapeHtml(item.status)}</span></td>
        <td class="admin-only">${action}</td>
      </tr>
    `;
  }).join("") : `<tr><td colspan="5" class="empty">No hour records yet.</td></tr>`;
}

function setupHoursPage() {
  const user = currentUser();
  const form = document.querySelector("#hoursForm");
  const table = document.querySelector("#hoursTable");
  if (!user || !table) return;

  form?.addEventListener("submit", async event => {
    event.preventDefault();
    if (user.role !== "student") return;
    const opportunityId = Number(document.querySelector("#activityName").value);
    const opportunity = state.opportunities.find(item => item.id === opportunityId);
    if (!opportunity) {
      showToast("Select an activity from your applications.");
      return;
    }
    try {
      await dataSource.submitHours({
        studentId: user.id,
        opportunityId,
        activity: opportunity.event,
        amount: Number(document.querySelector("#hoursContributed").value),
        note: document.querySelector("#hoursNote").value.trim() || "Submitted for NGO review"
      });
      form.reset();
      renderHours();
      showToast("Hours submitted for approval.");
    } catch (error) {
      showToast(error.message);
    }
  });

  table.addEventListener("click", async event => {
    const button = event.target.closest("button[data-hours-action]");
    if (!button || user.role !== "admin") return;
    try {
      await dataSource.reviewHours(Number(button.dataset.id), button.dataset.hoursAction, user.id);
      renderHours();
      showToast(button.dataset.hoursAction === "approve" ? "Hours approved." : "Hours rejected.");
    } catch (error) {
      showToast(error.message);
    }
  });
}

function studentRankings() {
  return state.users.filter(user => user.role === "student").map(user => ({
    id: user.id,
    name: user.fullName,
    referenceId: user.referenceId,
    hours: state.hours.filter(item => item.studentId === user.id && item.status === "approved")
      .reduce((sum, item) => sum + Number(item.amount), 0)
  })).sort((a, b) => b.hours - a.hours || a.name.localeCompare(b.name));
}

function renderLeaderboard() {
  const rankings = studentRankings();
  const podium = document.querySelector("#leaderPodium");
  const table = document.querySelector("#leaderboardTable");
  if (!podium || !table) return;
  podium.innerHTML = rankings.slice(0, 3).map((student, index) => `
    <article class="podium-card">
      <div class="podium-rank">#${index + 1}</div>
      <strong>${escapeHtml(student.name)}</strong>
      <span>${student.hours} verified hours</span>
    </article>
  `).join("") || `<div class="empty">No ranked students yet.</div>`;
  table.innerHTML = rankings.map((student, index) => `
    <tr><td>#${index + 1}</td><td>${escapeHtml(student.name)}</td><td>${escapeHtml(student.referenceId)}</td><td>${student.hours}</td></tr>
  `).join("");
}

function exportLeaderboard() {
  const rows = [["Rank", "Student", "Student ID", "Verified hours"],
    ...studentRankings().map((student, index) => [index + 1, student.name, student.referenceId, student.hours])];
  const csv = rows.map(row => row.map(value => `"${String(value).replaceAll('"', '""')}"`).join(",")).join("\r\n");
  const link = document.createElement("a");
  link.href = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
  link.download = `e-sukarelawan-leaderboard-${new Date().toISOString().slice(0, 10)}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
}

function setupAuthPages() {
  const loginRole = document.querySelector("#loginRole");
  const studentIdField = document.querySelector(".student-id-field");
  const loginStudentId = document.querySelector("#loginStudentId");

  function requireFilled(fields) {
    const missing = fields.find(({ value }) => !String(value || "").trim());
    if (!missing) return false;
    showToast(`${missing.label} is required.`);
    missing.element?.focus();
    return true;
  }

  function refreshStudentIdField() {
    if (!loginRole || !studentIdField) return;
    const isStudent = loginRole.value === "student";
    studentIdField.classList.toggle("hidden", !isStudent);
    if (loginStudentId) loginStudentId.required = isStudent;
  }

  loginRole?.addEventListener("change", refreshStudentIdField);
  refreshStudentIdField();

  document.querySelector("#loginForm")?.addEventListener("submit", async event => {
    event.preventDefault();
    const roleInput = document.querySelector("#loginRole");
    const emailInput = document.querySelector("#loginEmail");
    const passwordInput = document.querySelector("#loginPassword");
    const studentId = loginStudentId?.value.trim() || "";
    const payload = {
      role: roleInput.value,
      email: emailInput.value.trim().toLowerCase(),
      password: passwordInput.value
    };
    const loginFields = [
      { label: "Role", value: payload.role, element: roleInput },
      { label: "Email", value: payload.email, element: emailInput },
      { label: "Password", value: payload.password, element: passwordInput }
    ];
    if (payload.role === "student") {
      loginFields.splice(2, 0, { label: "Student ID", value: studentId, element: loginStudentId });
    }
    if (requireFilled(loginFields)) return;

    try {
      await dataSource.login(payload);
      if (payload.role === "student" && studentId) {
        const user = currentUser();
        if (user) {
          user.referenceId = studentId;
          dataSource.save();
        }
        sessionStorage.setItem(UNIVERSITY_SESSION_KEY, studentId);
      } else {
        sessionStorage.removeItem(UNIVERSITY_SESSION_KEY);
      }
      go("dashboard.html");
    } catch (error) {
      showToast(error.message);
    }
  });

  document.querySelector("#registerForm")?.addEventListener("submit", async event => {
    event.preventDefault();
    const nameInput = document.querySelector("#registerName");
    const emailInput = document.querySelector("#registerEmail");
    const roleInput = document.querySelector("#registerRole");
    const idInput = document.querySelector("#registerId");
    const passwordInput = document.querySelector("#registerPassword");
    const payload = {
      fullName: nameInput.value.trim(),
      email: emailInput.value.trim().toLowerCase(),
      role: roleInput.value,
      referenceId: idInput.value.trim(),
      password: passwordInput.value
    };
    if (requireFilled([
      { label: "Full name", value: payload.fullName, element: nameInput },
      { label: "Email", value: payload.email, element: emailInput },
      { label: "Role", value: payload.role, element: roleInput },
      { label: "Student ID / NGO code", value: payload.referenceId, element: idInput },
      { label: "Password", value: payload.password, element: passwordInput }
    ])) return;

    try {
      await dataSource.register(payload);
      go("dashboard.html");
    } catch (error) {
      showToast(error.message);
    }
  });
}

function renderShell() {
  const sidebar = document.querySelector("#sidebar");
  const topbar = document.querySelector("#topbar");
  const user = currentUser();
  if (!sidebar || !topbar || !user) return;
  const university = universityForUser(user);

  const links = [
    ["dashboard", "dashboard.html", "🏠", "Dashboard"],
    ["opportunities", "opportunities.html", "🤝", "Volunteer opportunities"],
    ["hours", "hours.html", "⏱️", "Volunteer hours"],
    ["leaderboard", "leaderboard.html", "🏆", "Leaderboard"]
  ];

  sidebar.innerHTML = `
    <div class="side-brand">
      <span class="brand-mark"></span>
      <div><strong>E-Sukarelawan</strong><span>Sukarelawan, Masyarakat Berdaya</span></div>
    </div>
    <nav class="side-nav">
      ${links.map(([page, href, icon, label]) => `
        <a class="nav-link ${pageName === page ? "active" : ""}" href="${href}">
          <span class="nav-icon">${icon}</span><span>${label}</span>
        </a>
      `).join("")}
    </nav>
    <div class="side-user">
      <strong>${escapeHtml(user.fullName)}</strong>
      <span>${user.role === "admin" ? "NGO Admin" : escapeHtml(user.referenceId)}</span>
    </div>
    ${isStudent ? `
      <div class="university-mark">
        <span class="crest university-logo university-logo-${categoryClass(university.code)}" data-logo="${escapeHtml(university.code)}"></span>
        <div><strong>${escapeHtml(university.name)}</strong><span>${escapeHtml(university.motto)}</span></div>
      </div>
    ` : ""}
  `;

  topbar.innerHTML = `
    <div class="system-state">${dataSource.mode === "api" ? "Server connected" : "Demo mode"}</div>
    <button class="bell-btn theme-toggle" type="button" id="themeToggleBtn" aria-label="Switch to dark theme" aria-pressed="false">
      <span class="theme-glyph">D</span><span class="theme-dot"></span>
    </button>
    <div class="top-profile">
      <div class="avatar">${initials(user.fullName)}</div>
      <div class="profile-copy"><strong>${escapeHtml(user.fullName)}</strong><span>${user.role === "admin" ? "Coordinator" : "Student"} - ${escapeHtml(university.code)}</span></div>
      <span class="profile-caret">⌄</span>
    </div>
    <button class="btn secondary logout-btn" type="button" id="logoutBtn">Logout</button>
  `;
  applyTheme();
  document.querySelector("#themeToggleBtn")?.addEventListener("click", toggleTheme);
  document.querySelector("#logoutBtn").addEventListener("click", async () => {
    await dataSource.logout();
    go("login.html");
  });
  setupSidebarToggle();
}

function renderDashboard() {
  const user = currentUser();
  if (!user) return;

  document.querySelector("#dashboardGreeting").textContent = user.role === "admin"
    ? `Selamat datang kembali, ${user.fullName}.`
    : `Selamat datang kembali, ${user.fullName}.`;

  const relevantHours = user.role === "student"
    ? state.hours.filter(item => item.studentId === user.id)
    : state.hours;
  const verifiedHours = relevantHours.filter(item => item.status === "approved")
    .reduce((sum, item) => sum + Number(item.amount), 0);
  const pendingHours = relevantHours.filter(item => item.status === "pending").length;
  const activeEvents = state.opportunities.filter(item => item.status !== "closed").length;
  const rankings = studentRankings();
  const rank = user.role === "student" ? rankings.findIndex(item => item.id === user.id) + 1 : 0;

  document.querySelector("#metricHours").textContent = formatHours(verifiedHours);
  document.querySelector("#metricEvents").textContent = activeEvents;
  document.querySelector("#metricPending").textContent = pendingHours;
  document.querySelector("#metricRank").textContent = rank ? `#${rank}` : "—";

  const metricSmall = document.querySelectorAll(".metric small");
  if (metricSmall[0]) metricSmall[0].textContent = "+12.5 this month";
  if (metricSmall[1]) metricSmall[1].textContent = user.role === "student" ? "You're registered" : "Programmes active";
  if (metricSmall[2]) metricSmall[2].textContent = "Awaiting approval";
  if (metricSmall[3]) metricSmall[3].textContent = user.role === "student" ? "Top 5% this month" : "Verified contributors";

  document.querySelector("#dispatchList").innerHTML = state.opportunities
    .filter(item => item.status !== "closed")
    .slice(0, 3)
    .map(item => `
      <article class="event-row">
        <div class="event-thumb thumb-${categoryClass(item.category)}"></div>
        <div class="event-copy">
          <strong>${escapeHtml(item.event)}</strong>
          <span class="event-meta">⌖ ${escapeHtml(item.location)}</span>
          <span class="event-meta">▣ ${opportunityTime(item)}</span>
          <span class="tag ${item.category.toLowerCase().replace(/\s+/g, "-")}">${escapeHtml(item.category)}</span>
        </div>
        <a class="btn primary event-btn" href="opportunities.html">View details</a>
      </article>
    `).join("") || `<div class="empty">No active opportunities.</div>`;

  const queueTitle = document.querySelector("#queueTitle");
  const queueCopy = document.querySelector("#queueCopy");
  const queueList = document.querySelector("#queueList");

  queueTitle.textContent = user.role === "admin" ? "Approval queue" : "Approval queue";
  queueCopy.innerHTML = `<span class="queue-tabs"><b>Hours to approve</b><span>Applications to review <em>${state.applications.filter(item => item.status === "pending").length}</em></span></span>`;

  const queueItems = state.hours
    .filter(item => user.role === "admin" ? item.status === "pending" : item.studentId !== user.id)
    .slice(0, 5);
  queueList.innerHTML = queueItems.map((item, index) => `
    <article class="queue-row">
      <div class="queue-avatar">${initials(userName(item.studentId, item.studentName))}</div>
      <div>
        <strong>${escapeHtml(userName(item.studentId, item.studentName))}</strong>
        <span>${escapeHtml(item.activity)}</span>
        <small>${escapeHtml(item.note || "Pending review")}</small>
      </div>
      <b>${formatHours(item.amount)} hrs</b>
      <span class="tag ${index > 2 ? "in-review" : item.status}">${index > 2 ? "In review" : escapeHtml(item.status)}</span>
      <span class="queue-arrow">&rsaquo;</span>
    </article>
  `).join("") || `<div class="empty">Nothing needs review.</div>`;
}

function renderHours() {
  const user = currentUser();
  const table = document.querySelector("#hoursTable");
  if (!user || !table) return;
  const visibleHours = user.role === "student"
    ? state.hours.filter(item => item.studentId === user.id)
    : state.hours;
  document.querySelector("#hoursIntro").textContent = user.role === "admin"
    ? "Review submitted service hours and keep records verified."
    : "Submit completed service and track approval.";
  document.querySelector("#hoursLogCopy").textContent = user.role === "admin"
    ? "All student submissions."
    : "Your approved and pending records.";

  table.innerHTML = visibleHours.length ? visibleHours.map(item => {
    const action = user.role === "admin" && item.status === "pending"
      ? `<div class="row-actions"><button class="btn blue" data-hours-action="approve" data-id="${item.id}" type="button">Approve</button><button class="btn danger" data-hours-action="reject" data-id="${item.id}" type="button">Reject</button></div>`
      : user.role === "admin" ? "Reviewed" : "";
    return `
      <tr>
        <td>${escapeHtml(userName(item.studentId, item.studentName))}</td>
        <td>${escapeHtml(item.activity)}<div class="cell-note">${escapeHtml(item.note)}</div></td>
        <td>${formatHours(item.amount)}</td>
        <td><span class="tag ${item.status}">${escapeHtml(item.status)}</span></td>
        <td class="admin-only">${action}</td>
      </tr>
    `;
  }).join("") : `<tr><td colspan="5" class="empty">No hour records yet.</td></tr>`;
}

function renderLeaderboard() {
  const rankings = studentRankings();
  const podium = document.querySelector("#leaderPodium");
  const table = document.querySelector("#leaderboardTable");
  if (!podium || !table) return;
  podium.innerHTML = rankings.slice(0, 3).map((student, index) => `
    <article class="podium-card">
      <div class="podium-rank">#${index + 1}</div>
      <strong>${escapeHtml(student.name)}</strong>
      <span>${formatHours(student.hours)} verified hours</span>
    </article>
  `).join("") || `<div class="empty">No ranked students yet.</div>`;
  table.innerHTML = rankings.map((student, index) => `
    <tr><td>#${index + 1}</td><td>${escapeHtml(student.name)}</td><td>${escapeHtml(student.referenceId)}</td><td>${formatHours(student.hours)}</td></tr>
  `).join("");
}

function renderShell() {
  const sidebar = document.querySelector("#sidebar");
  const topbar = document.querySelector("#topbar");
  const user = currentUser();
  if (!sidebar || !topbar || !user) return;
  const university = universityForUser(user);
  const isStudent = user.role === "student";

  const links = [
    ["dashboard", "dashboard.html", "🏠", "Dashboard"],
    ["opportunities", "opportunities.html", "🤝", "Volunteer opportunities"],
    ["hours", "hours.html", "⏱️", "Volunteer hours"],
    ["leaderboard", "leaderboard.html", "🏆", "Leaderboard"],
    ["feedback", "feedback.html", "💬", "Feedback"],
    ["profile", "profile.html", "👤", "Profile"]
  ];

  sidebar.innerHTML = `
    <div class="side-brand">
      <span class="brand-mark"></span>
      <div><strong>E-Sukarelawan</strong><span>Sukarelawan, Masyarakat Berdaya</span></div>
    </div>
    <nav class="side-nav">
      ${links.map(([page, href, icon, label]) => `
        <a class="nav-link ${pageName === page ? "active" : ""}" href="${href}">
          <span class="nav-icon">${icon}</span><span>${label}</span>
        </a>
      `).join("")}
    </nav>
    <div class="side-user">
      <strong>${escapeHtml(user.fullName)}</strong>
      <span>${user.role === "admin" ? "NGO Admin" : escapeHtml(user.referenceId)}</span>
    </div>
    ${isStudent ? `
      <div class="university-mark">
        <span class="crest university-logo university-logo-${categoryClass(university.code)}" data-logo="${escapeHtml(university.code)}"></span>
        <div><strong>${escapeHtml(university.name)}</strong><span>${escapeHtml(university.motto)}</span></div>
      </div>
    ` : ""}
  `;

  topbar.innerHTML = `
    <div class="system-state">${dataSource.mode === "api" ? "Server connected" : "Demo mode"}</div>
    <button class="bell-btn theme-toggle" type="button" id="themeToggleBtn" aria-label="Switch to dark theme" aria-pressed="false">
      <span class="theme-glyph">D</span><span class="theme-dot"></span>
    </button>
    <a class="top-profile" href="profile.html" aria-label="Open profile page">
      ${avatarMarkup(user)}
      <div class="profile-copy"><strong>${escapeHtml(user.fullName)}</strong><span>${isStudent ? `Student - ${escapeHtml(university.code)}` : "Coordinator"}</span></div>
    </a>
    <button class="btn secondary logout-btn" type="button" id="logoutBtn">Logout</button>
  `;
  applyTheme();
  document.querySelector("#themeToggleBtn")?.addEventListener("click", toggleTheme);
  document.querySelector("#logoutBtn").addEventListener("click", async () => {
    await dataSource.logout();
    go("login.html");
  });
  setupSidebarToggle();
}

function renderShell() {
  const sidebar = document.querySelector("#sidebar");
  const topbar = document.querySelector("#topbar");
  const user = currentUser();
  if (!sidebar || !topbar || !user) return;

  const university = universityForUser(user);
  const isStudent = user.role === "student";
  const displayName = displayNameForUser(user);
  const links = [
    ["dashboard", "dashboard.html", "🏠", "Dashboard"],
    ["opportunities", "opportunities.html", "🤝", "Volunteer opportunities"],
    ["hours", "hours.html", "⏱️", "Volunteer hours"],
    ["leaderboard", "leaderboard.html", "🏆", "Leaderboard"],
    ["feedback", "feedback.html", "💬", "Feedback"],
    ["profile", "profile.html", "👤", "Profile"]
  ];

  sidebar.innerHTML = `
    <div class="side-brand">
      <span class="brand-mark"></span>
      <div><strong>E-Sukarelawan</strong><span>Sukarelawan, Masyarakat Berdaya</span></div>
    </div>
    <nav class="side-nav">
      ${links.map(([page, href, icon, label]) => `
        <a class="nav-link ${pageName === page ? "active" : ""}" href="${href}">
          <span class="nav-icon">${icon}</span><span>${label}</span>
        </a>
      `).join("")}
    </nav>
    <div class="side-user">
      <strong>${escapeHtml(displayName)}</strong>
      <span>${user.role === "admin" ? "NGO Admin" : escapeHtml(user.referenceId)}</span>
    </div>
    ${isStudent ? `
      <div class="university-mark">
        <span class="crest university-logo university-logo-${categoryClass(university.code)}" data-logo="${escapeHtml(university.code)}"></span>
        <div><strong>${escapeHtml(university.name)}</strong><span>${escapeHtml(university.motto)}</span></div>
      </div>
    ` : ""}
  `;

  topbar.innerHTML = `
    <div class="system-state">${dataSource.mode === "api" ? "Server connected" : "Demo mode"}</div>
    <button class="bell-btn theme-toggle" type="button" id="themeToggleBtn" aria-label="Switch to dark theme" aria-pressed="false">
      <span class="theme-glyph">D</span><span class="theme-dot"></span>
    </button>
    <a class="top-profile" href="profile.html" aria-label="Open profile page">
      ${avatarMarkup({ ...user, fullName: displayName })}
      <div class="profile-copy"><strong>${escapeHtml(displayName)}</strong><span>${isStudent ? `Student - ${escapeHtml(university.code)}` : "Coordinator"}</span></div>
    </a>
    <button class="btn secondary logout-btn" type="button" id="logoutBtn">Logout</button>
  `;
  applyTheme();
  document.querySelector("#themeToggleBtn")?.addEventListener("click", toggleTheme);
  document.querySelector("#logoutBtn").addEventListener("click", async () => {
    await dataSource.logout();
    go("login.html");
  });
  setupSidebarToggle();
}

let profilePhotoDraft = null;
const PROFILE_PHOTO_MAX_FILE_SIZE = 5 * 1024 * 1024;
const PROFILE_PHOTO_MAX_EDGE = 900;
const PROFILE_PHOTO_TARGET_LENGTH = 900000;

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.addEventListener("load", () => resolve(String(reader.result || "")));
    reader.addEventListener("error", () => reject(new Error("Could not read the selected picture.")));
    reader.readAsDataURL(file);
  });
}

function loadImage(dataUrl) {
  return new Promise((resolve, reject) => {
    const image = new Image();
    image.addEventListener("load", () => resolve(image));
    image.addEventListener("error", () => reject(new Error("Could not prepare the selected picture.")));
    image.src = dataUrl;
  });
}

async function prepareProfilePhoto(file) {
  const original = await readFileAsDataUrl(file);
  const image = await loadImage(original);
  const scale = Math.min(1, PROFILE_PHOTO_MAX_EDGE / Math.max(image.naturalWidth, image.naturalHeight));
  const width = Math.max(1, Math.round(image.naturalWidth * scale));
  const height = Math.max(1, Math.round(image.naturalHeight * scale));
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext("2d");
  context.fillStyle = "#ffffff";
  context.fillRect(0, 0, width, height);
  context.drawImage(image, 0, 0, width, height);

  let quality = 0.82;
  let compressed = canvas.toDataURL("image/jpeg", quality);
  while (compressed.length > PROFILE_PHOTO_TARGET_LENGTH && quality > 0.55) {
    quality -= 0.08;
    compressed = canvas.toDataURL("image/jpeg", quality);
  }
  return compressed.length < original.length ? compressed : original;
}

function renderProfile() {
  const user = currentUser();
  const form = document.querySelector("#profileForm");
  if (!user || !form) return;

  const profile = profileForUser(user);
  const university = universityForUser(user);
  const displayName = displayNameForUser(user);
  const rankings = studentRankings();
  const rank = user.role === "student" ? rankings.findIndex(item => item.id === user.id) + 1 : 0;
  const verifiedHours = state.hours
    .filter(item => item.studentId === user.id && item.status === "approved")
    .reduce((sum, item) => sum + Number(item.amount), 0);
  const photo = profilePhotoDraft === null ? profile.photo : profilePhotoDraft;
  const roleLabel = user.role === "admin" ? "Coordinator" : "Student";
  const synopsis = profile.bio || "Add your volunteer profile summary to complete the poster.";
  const isStudent = user.role === "student";

  document.querySelector("#profileDisplayName").textContent = displayName;
  document.querySelector("#profileDisplayMeta").textContent = isStudent ? `${roleLabel} - ${university.name}` : roleLabel;
  document.querySelector("#profileSummaryHours").textContent = formatHours(verifiedHours);
  document.querySelector("#profileSummaryRank").textContent = rank ? `#${rank}` : "-";
  document.querySelector("#profilePhotoPreview").innerHTML = photo
    ? `<img src="${escapeHtml(photo)}" alt="${escapeHtml(user.fullName)} profile picture">`
    : `<span>${initials(user.fullName)}</span>`;
  const metaId = document.querySelector("#profileMetaId");
  const metaUniversity = document.querySelector("#profileMetaUniversity");
  const metaRole = document.querySelector("#profileMetaRole");
  const metaIdLabel = metaId?.closest("span")?.querySelector("b");
  const metaUniversityLabel = metaUniversity?.closest("span")?.querySelector("b");
  const synopsisNode = document.querySelector("#profileSynopsis");
  if (metaIdLabel) metaIdLabel.textContent = isStudent ? "Student ID" : "Staff ID";
  if (metaId) metaId.textContent = user.referenceId || "-";
  if (metaUniversityLabel) metaUniversityLabel.textContent = isStudent ? "University" : "Workspace";
  if (metaUniversity) metaUniversity.textContent = isStudent ? university.name : "Admin server";
  if (metaRole) metaRole.textContent = roleLabel;
  if (synopsisNode) synopsisNode.textContent = synopsis;

  document.querySelector("#profileName").value = user.fullName || "";
  document.querySelector("#profileReferenceId").value = user.referenceId || "";
  document.querySelector("#profileEmail").value = user.email || "";
  document.querySelector("#profilePhone").value = profile.phone || "";
  document.querySelector("#profileFaculty").value = profile.faculty || "";
  document.querySelector("#profileProgramme").value = profile.programme || "";
  document.querySelector("#profileBio").value = profile.bio || "";
}

function setupProfilePage() {
  const form = document.querySelector("#profileForm");
  const photoInput = document.querySelector("#profilePhotoInput");
  const removeButton = document.querySelector("#removeProfilePhotoBtn");
  if (!form) return;

  async function saveProfileChanges(successMessage) {
    const user = currentUser();
    if (!user) return;
    const profile = profileForUser(user);

    const nextProfile = {
      phone: document.querySelector("#profilePhone").value.trim(),
      faculty: document.querySelector("#profileFaculty").value.trim(),
      programme: document.querySelector("#profileProgramme").value.trim(),
      bio: document.querySelector("#profileBio").value.trim(),
      photo: profilePhotoDraft === null ? profile.photo : profilePhotoDraft
    };
    const payload = {
      fullName: document.querySelector("#profileName").value.trim(),
      referenceId: document.querySelector("#profileReferenceId").value.trim(),
      email: document.querySelector("#profileEmail").value.trim().toLowerCase(),
      profile: nextProfile
    };

    await dataSource.updateProfile(user.id, payload);
    const updatedUser = currentUser() || user;
    Object.assign(updatedUser, payload);
    updatedUser.profile = nextProfile;
    if (updatedUser.role === "admin") updatedUser.ngoName = payload.fullName;
    if (updatedUser.role === "student" && updatedUser.referenceId) {
      sessionStorage.setItem(UNIVERSITY_SESSION_KEY, updatedUser.referenceId);
    }
    profilePhotoDraft = null;
    setRoleClasses();
    renderShell();
    renderProfile();
    setupProfilePage();
    showToast(successMessage);
  }

  photoInput?.addEventListener("change", async event => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      showToast("Please choose an image file.");
      event.target.value = "";
      return;
    }
    if (file.size > PROFILE_PHOTO_MAX_FILE_SIZE) {
      showToast("Picture must be under 5 MB.");
      event.target.value = "";
      return;
    }
    try {
      showToast("Preparing profile picture...");
      profilePhotoDraft = await prepareProfilePhoto(file);
      const user = currentUser();
      const preview = document.querySelector("#profilePhotoPreview");
      if (preview && user) {
        preview.innerHTML = `<img src="${escapeHtml(profilePhotoDraft)}" alt="${escapeHtml(user.fullName)} profile picture">`;
      }
      await saveProfileChanges("Profile picture saved to database.");
    } catch (error) {
      event.target.value = "";
      showToast(error.message);
    }
  });

  removeButton?.addEventListener("click", async () => {
    profilePhotoDraft = "";
    if (photoInput) photoInput.value = "";
    try {
      await saveProfileChanges("Profile picture removed from database.");
    } catch (error) {
      showToast(error.message);
    }
  });

  form.addEventListener("submit", async event => {
    event.preventDefault();
    try {
      await saveProfileChanges("Profile updated.");
    } catch (error) {
      showToast(error.message);
    }
  });
}

function renderFeedback() {
  const user = currentUser();
  const listNode = document.querySelector("#feedbackList");
  const formPanel = document.querySelector("#feedbackForm");
  const emptyNode = document.querySelector("#feedbackEmpty");
  if (!user || !listNode) return;

  const isAdmin = user.role === "admin";
  const feedbackItems = (state.feedback || [])
    .filter(item => isAdmin || item.studentId === user.id)
    .slice()
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));

  const hasAdminReply = item => Boolean(String(item.adminReply || "").trim());
  const openCount = feedbackItems.filter(item => !hasAdminReply(item)).length;
  const repliedCount = feedbackItems.filter(hasAdminReply).length;
  const totalNode = document.querySelector("#feedbackTotal");
  const openNode = document.querySelector("#feedbackOpen");
  const repliedNode = document.querySelector("#feedbackReplied");
  if (totalNode) totalNode.textContent = feedbackItems.length;
  if (openNode) openNode.textContent = openCount;
  if (repliedNode) repliedNode.textContent = repliedCount;

  if (formPanel) formPanel.hidden = isAdmin;
  if (emptyNode) emptyNode.hidden = feedbackItems.length > 0;

  listNode.innerHTML = feedbackItems.map(item => {
    const student = userName(item.studentId, "Student");
    const admin = item.adminId ? userName(item.adminId, "Admin") : "Awaiting admin";
    const replied = hasAdminReply(item);
    const statusLabel = replied ? "replied" : item.status || "open";
    return `
      <article class="feedback-card ${replied ? "replied" : "open"}">
        <div class="feedback-card-head">
          <div class="queue-avatar">${initials(student)}</div>
          <div>
            <span class="section-tag">${escapeHtml(statusLabel)}</span>
            <h3>${escapeHtml(item.subject)}</h3>
            <p>${escapeHtml(student)} - ${formatDateTime(item.createdAt)}</p>
          </div>
        </div>
        <p class="feedback-message">${escapeHtml(item.message)}</p>
        <div class="feedback-reply ${replied ? "" : "pending"}">
          <strong>${replied ? `Admin reply from ${escapeHtml(admin)}` : "No admin reply yet"}</strong>
          <p>${replied ? escapeHtml(item.adminReply) : (isAdmin ? "Reply to this feedback below." : "Your admin has not replied yet. The reply will appear here once it is sent.")}</p>
          ${replied ? `<small>${formatDateTime(item.repliedAt)}</small>` : ""}
        </div>
        ${isAdmin ? `
          <form class="feedback-reply-form" data-feedback-id="${item.id}">
            <label class="field">Admin reply
              <textarea name="reply" required placeholder="Write a reply to ${escapeHtml(student)}">${replied ? escapeHtml(item.adminReply) : ""}</textarea>
            </label>
            <button class="btn primary" type="submit">${replied ? "Update reply" : "Send reply"}</button>
          </form>
        ` : ""}
      </article>
    `;
  }).join("");
}

function setupFeedbackPage() {
  const form = document.querySelector("#feedbackForm");
  const listNode = document.querySelector("#feedbackList");

  form?.addEventListener("submit", async event => {
    event.preventDefault();
    const user = currentUser();
    if (!user) return;
    const payload = {
      studentId: user.id,
      subject: document.querySelector("#feedbackSubject").value.trim(),
      message: document.querySelector("#feedbackMessage").value.trim()
    };

    try {
      await dataSource.submitFeedback(payload);
      form.reset();
      renderFeedback();
      showToast("Feedback sent.");
    } catch (error) {
      showToast(error.message);
    }
  });

  listNode?.addEventListener("submit", async event => {
    const replyForm = event.target.closest(".feedback-reply-form");
    if (!replyForm) return;
    event.preventDefault();
    const admin = currentUser();
    const id = Number(replyForm.dataset.feedbackId);
    const reply = replyForm.elements.reply.value.trim();
    if (!admin || !id) return;

    try {
      await dataSource.replyFeedback(id, { adminId: admin.id, reply });
      renderFeedback();
      showToast("Reply saved.");
    } catch (error) {
      showToast(error.message);
    }
  });
}

function renderDashboard() {
  const user = currentUser();
  if (!user) return;

  document.querySelector("#dashboardGreeting").textContent = `Selamat datang kembali, ${displayNameForUser(user)}.`;

  const relevantHours = user.role === "student"
    ? state.hours.filter(item => item.studentId === user.id)
    : state.hours;
  const verifiedHours = relevantHours.filter(item => item.status === "approved")
    .reduce((sum, item) => sum + Number(item.amount), 0);
  const pendingHours = relevantHours.filter(item => item.status === "pending").length;
  const activeEvents = state.opportunities.filter(item => item.status !== "closed").length;
  const rankings = studentRankings();
  const rank = user.role === "student" ? rankings.findIndex(item => item.id === user.id) + 1 : 0;

  document.querySelector("#metricHours").textContent = formatHours(verifiedHours);
  document.querySelector("#metricEvents").textContent = activeEvents;
  document.querySelector("#metricPending").textContent = pendingHours;
  document.querySelector("#metricRank").textContent = rank ? `#${rank}` : "-";

  const metricSmall = document.querySelectorAll(".metric small");
  if (metricSmall[0]) metricSmall[0].textContent = "+12.5 this month";
  if (metricSmall[1]) metricSmall[1].textContent = user.role === "student" ? "You're registered" : "Programmes active";
  if (metricSmall[2]) metricSmall[2].textContent = "Awaiting approval";
  if (metricSmall[3]) metricSmall[3].textContent = user.role === "student" ? "Top 5% this month" : "Verified contributors";

  document.querySelector("#dispatchList").innerHTML = state.opportunities
    .filter(item => item.status !== "closed")
    .slice(0, 3)
    .map(item => `
      <article class="event-row">
        <div class="event-thumb thumb-${categoryClass(item.category)}"></div>
        <div class="event-copy">
          <strong>${escapeHtml(item.event)}</strong>
          <span class="event-meta">* ${escapeHtml(item.location)}</span>
          <span class="event-meta">[ ] ${opportunityTime(item)}</span>
          <span class="tag ${categoryClass(item.category)}">${escapeHtml(item.category)}</span>
        </div>
        <a class="btn primary event-btn" href="opportunities.html">View details</a>
      </article>
    `).join("") || `<div class="empty">No active opportunities.</div>`;

  const queueTitle = document.querySelector("#queueTitle");
  const queueCopy = document.querySelector("#queueCopy");
  const queueList = document.querySelector("#queueList");
  const pendingApplications = state.applications.filter(item => item.status === "pending");
  const pendingHoursQueue = state.hours.filter(item => item.status === "pending");

  queueTitle.textContent = user.role === "admin" ? "Approval queue" : "My applications";
  queueCopy.innerHTML = user.role === "admin"
    ? `<span class="queue-tabs" role="tablist" aria-label="Approval queue filters">
        <button class="queue-tab-button ${activeQueueTab === "hours" ? "active" : ""}" data-queue-tab="hours" type="button" role="tab" aria-selected="${activeQueueTab === "hours"}">Hours to approve <em>${pendingHoursQueue.length}</em></button>
        <button class="queue-tab-button ${activeQueueTab === "applications" ? "active" : ""}" data-queue-tab="applications" type="button" role="tab" aria-selected="${activeQueueTab === "applications"}">Applications to review <em>${pendingApplications.length}</em></button>
      </span>`
    : "Your latest volunteer applications.";

  if (user.role === "student") {
    const applications = state.applications.filter(application => application.studentId === user.id);
    queueList.innerHTML = applications.map(application => {
      const opportunity = state.opportunities.find(item => item.id === application.opportunityId);
      return `
        <article class="queue-row">
          <div class="queue-avatar">${initials(opportunity?.event || "EV")}</div>
          <div>
            <strong>${escapeHtml(opportunity?.event || "Volunteer programme")}</strong>
            <span>${escapeHtml(opportunity?.location || "Location pending")}</span>
            <small>Applied on ${formatDate(application.applicationDate || new Date().toISOString().slice(0, 10))}</small>
          </div>
          <b>${escapeHtml(opportunity?.category || "Activity")}</b>
          <span class="tag ${application.status}">${escapeHtml(application.status)}</span>
          <span class="queue-arrow" aria-hidden="true">&rsaquo;</span>
        </article>
      `;
    }).join("") || `<div class="empty">No applications yet.</div>`;
    return;
  }

  if (activeQueueTab === "applications") {
    queueList.innerHTML = pendingApplications.slice(0, 5).map(application => {
      const opportunity = state.opportunities.find(item => item.id === application.opportunityId);
      const applicant = userName(application.studentId, "Student applicant");
      return `
        <button class="queue-row queue-action-row" data-review-type="application" data-id="${application.id}" type="button">
          <div class="queue-avatar">${initials(applicant)}</div>
          <div>
            <strong>${escapeHtml(applicant)}</strong>
            <span>${escapeHtml(opportunity?.event || "Volunteer application")}</span>
            <small>Applied on ${formatDate(application.applicationDate || new Date().toISOString().slice(0, 10))}</small>
          </div>
          <b>Application</b>
          <span class="tag ${application.status}">${escapeHtml(application.status)}</span>
          <span class="queue-arrow" aria-hidden="true">&rsaquo;</span>
        </button>
      `;
    }).join("") || `<div class="empty">No applications need review.</div>`;
    return;
  }

  queueList.innerHTML = pendingHoursQueue.slice(0, 5).map(item => `
    <button class="queue-row queue-action-row" data-review-type="hours" data-id="${item.id}" type="button">
      <div class="queue-avatar">${initials(userName(item.studentId, item.studentName))}</div>
      <div>
        <strong>${escapeHtml(userName(item.studentId, item.studentName))}</strong>
        <span>${escapeHtml(item.activity)}</span>
        <small>${escapeHtml(item.note || "Pending review")}</small>
      </div>
      <b>${formatHours(item.amount)} hrs</b>
      <span class="tag ${item.status}">${escapeHtml(item.status)}</span>
      <span class="queue-arrow" aria-hidden="true">&rsaquo;</span>
    </button>
  `).join("") || `<div class="empty">Nothing needs review.</div>`;
}

function ensureReviewModal() {
  let modal = document.querySelector("#reviewModal");
  if (modal) return modal;

  modal = document.createElement("div");
  modal.id = "reviewModal";
  modal.className = "review-modal hidden";
  modal.innerHTML = `
    <div class="review-backdrop" data-review-close></div>
    <article class="review-dialog" role="dialog" aria-modal="true" aria-labelledby="reviewModalTitle">
      <button class="review-close" data-review-close type="button" aria-label="Close review panel">x</button>
      <span class="section-tag" id="reviewModalType">Review</span>
      <h2 id="reviewModalTitle">Application review</h2>
      <p id="reviewModalIntro">Check the submission details before making a decision.</p>
      <div class="review-detail-grid" id="reviewDetails"></div>
      <div class="review-actions">
        <button class="btn danger" data-review-action="reject" type="button">Reject</button>
        <button class="btn primary" data-review-action="approve" type="button">Approve</button>
      </div>
    </article>
  `;
  document.body.appendChild(modal);
  return modal;
}

function detailBlock(label, value) {
  return `<div class="review-detail"><span>${escapeHtml(label)}</span><strong>${escapeHtml(value || "-")}</strong></div>`;
}

function openReviewModal(type, id) {
  const modal = ensureReviewModal();
  const isApplication = type === "application";
  const record = isApplication
    ? state.applications.find(item => item.id === id)
    : state.hours.find(item => item.id === id);
  if (!record) {
    showToast("Review item not found.");
    return;
  }

  const opportunity = state.opportunities.find(item => item.id === record.opportunityId);
  const applicant = userName(record.studentId, record.studentName || "Student applicant");

  modal.dataset.reviewType = type;
  modal.dataset.reviewId = String(id);
  modal.querySelector("#reviewModalType").textContent = isApplication ? "Applications" : "Hours";
  modal.querySelector("#reviewModalTitle").textContent = isApplication ? "Application review" : "Volunteer hours review";
  modal.querySelector("#reviewModalIntro").textContent = isApplication
    ? "Review the student application before approving the programme seat."
    : "Review the submitted service hours before updating the volunteer record.";
  modal.querySelector("#reviewDetails").innerHTML = isApplication
    ? [
        detailBlock("Student", applicant),
        detailBlock("Programme", opportunity?.event || "Volunteer programme"),
        detailBlock("Applied", formatDate(record.applicationDate || new Date().toISOString().slice(0, 10))),
        detailBlock("Location", opportunity?.location || "Location pending"),
        detailBlock("Category", opportunity?.category || "Activity"),
        detailBlock("Status", record.status)
      ].join("")
    : [
        detailBlock("Student", applicant),
        detailBlock("Programme", record.activity || opportunity?.event || "Volunteer programme"),
        detailBlock("Submitted note", record.note || "Pending review"),
        detailBlock("Hours", `${formatHours(record.amount)} hrs`),
        detailBlock("Status", record.status),
        detailBlock("Programme date", opportunity?.date ? formatDate(opportunity.date) : "Date pending")
      ].join("");

  modal.classList.remove("hidden");
  modal.querySelector("[data-review-action='approve']")?.focus();
}

function closeReviewModal() {
  document.querySelector("#reviewModal")?.classList.add("hidden");
}

function setupDashboardPage() {
  const queueCopy = document.querySelector("#queueCopy");
  const queueList = document.querySelector("#queueList");
  if (!queueCopy || !queueList) return;

  queueCopy.addEventListener("click", event => {
    const button = event.target.closest("button[data-queue-tab]");
    if (!button) return;
    activeQueueTab = button.dataset.queueTab;
    renderDashboard();
  });

  queueList.addEventListener("click", event => {
    const button = event.target.closest("button[data-review-type]");
    if (!button) return;
    openReviewModal(button.dataset.reviewType, Number(button.dataset.id));
  });

  document.addEventListener("click", async event => {
    const closeButton = event.target.closest("[data-review-close]");
    if (closeButton) {
      closeReviewModal();
      return;
    }

    const actionButton = event.target.closest("button[data-review-action]");
    const modal = document.querySelector("#reviewModal");
    if (!actionButton || !modal || modal.classList.contains("hidden")) return;

    const type = modal.dataset.reviewType;
    const id = Number(modal.dataset.reviewId);
    const action = actionButton.dataset.reviewAction;
    const admin = currentUser();
    if (!admin) return;

    try {
      if (type === "application") {
        await dataSource.reviewApplication(id, action, admin.id);
      } else {
        await dataSource.reviewHours(id, action, admin.id);
      }
      closeReviewModal();
      renderDashboard();
      showToast(action === "approve" ? "Review approved." : "Review rejected.");
    } catch (error) {
      showToast(error.message);
    }
  });

  document.addEventListener("keydown", event => {
    if (event.key === "Escape") closeReviewModal();
  });
}

async function initialize() {
  applyTheme();
  dataSource = await chooseDataSource();
  dataSource.save();
  setupAuthPages();
  if (!protectPage()) return;
  setRoleClasses();
  renderShell();

  if (pageName === "dashboard") {
    renderDashboard();
    setupDashboardPage();
  }
  if (pageName === "opportunities") {
    renderOpportunities();
    setupOpportunityPage();
  }
  if (pageName === "hours") {
    renderActivityOptions();
    renderHours();
    setupHoursPage();
  }
  if (pageName === "leaderboard") {
    renderLeaderboard();
    document.querySelector("#exportBtn")?.addEventListener("click", exportLeaderboard);
  }
  if (pageName === "feedback") {
    renderFeedback();
    setupFeedbackPage();
  }
  if (pageName === "profile") {
    renderProfile();
    setupProfilePage();
  }
}

initialize();
