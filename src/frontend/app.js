/**
 * Social Network Analyser - Client Application Controller
 * Seamlessly connects the interactive web interface to the Java Backend.
 */

// ==========================================
// STATE MANAGEMENT
// ==========================================
const AppState = {
  users: [],
  friendships: [],
  stats: {
    totalUsers: 0,
    totalFriendships: 0,
    mostPopularUser: 'None',
    maxFriends: 0,
    averageFriends: 0,
    isolatedUsersCount: 0
  },
  selectedUser: null,
  canvasNodes: [],
  draggingNode: null,
  hoveredNode: null,
  dragOffset: { x: 0, y: 0 }
};

// ==========================================
// DOM ELEMENTS
// ==========================================
const DOM = {
  // Stat values
  statTotalUsers: document.getElementById('stat-total-users'),
  statTotalFriendships: document.getElementById('stat-total-friendships'),
  statPopularUser: document.getElementById('stat-popular-user'),
  statPopularCount: document.getElementById('stat-popular-count'),
  statAvgFriends: document.getElementById('stat-avg-friends'),
  statIsolatedCount: document.getElementById('stat-isolated-count'),

  // Buttons
  btnDemo: document.getElementById('btn-demo'),
  btnReset: document.getElementById('btn-reset'),
  btnViva: document.getElementById('btn-viva'),
  themeToggle: document.getElementById('theme-toggle'),
  btnRecenter: document.getElementById('btn-recenter'),

  // Forms
  formAddUser: document.getElementById('form-add-user'),
  inputUserName: document.getElementById('input-user-name'),
  formAddFriendship: document.getElementById('form-add-friendship'),
  selectFriend1: document.getElementById('select-friend-1'),
  selectFriend2: document.getElementById('select-friend-2'),

  // Tabs
  tabBtns: document.querySelectorAll('.tab-btn'),
  tabPanes: document.querySelectorAll('.tab-pane'),

  // Directory
  inputSearchUser: document.getElementById('input-search-user'),
  btnClearSearch: document.getElementById('btn-clear-search'),
  usersDirectoryGrid: document.getElementById('users-directory-grid'),
  directoryCountBadge: document.getElementById('directory-count-badge'),

  // Mutual Friends
  selectMutual1: document.getElementById('select-mutual-1'),
  selectMutual2: document.getElementById('select-mutual-2'),
  btnFindMutual: document.getElementById('btn-find-mutual'),
  mutualResultsContainer: document.getElementById('mutual-results-container'),

  // Suggestions
  selectSuggestionUser: document.getElementById('select-suggestion-user'),
  btnGetSuggestions: document.getElementById('btn-get-suggestions'),
  suggestionsResultsContainer: document.getElementById('suggestions-results-container'),

  // Analytics
  leaderboardTbody: document.getElementById('leaderboard-tbody'),
  metricDensity: document.getElementById('metric-density'),
  metricConnectedUsers: document.getElementById('metric-connected-users'),
  metricIsolatedUsers: document.getElementById('metric-isolated-users'),
  metricMaxFriends: document.getElementById('metric-max-friends'),
  metricAvgDegree: document.getElementById('metric-avg-degree'),

  // Canvas
  canvas: document.getElementById('networkCanvas'),

  // Modals
  userModal: document.getElementById('user-modal'),
  modalClose: document.getElementById('modal-close'),
  modalAvatar: document.getElementById('modal-avatar'),
  modalUserName: document.getElementById('modal-user-name'),
  modalUserConnections: document.getElementById('modal-user-connections'),
  modalFriendsList: document.getElementById('modal-friends-list'),
  modalFriendCount: document.getElementById('modal-friend-count'),
  modalBtnFindMutual: document.getElementById('modal-btn-find-mutual'),
  modalBtnGetSuggestions: document.getElementById('modal-btn-get-suggestions'),

  vivaModal: document.getElementById('viva-modal'),
  vivaModalClose: document.getElementById('viva-modal-close'),

  // Toast
  toastContainer: document.getElementById('toast-container')
};

// Canvas Context
let ctx = DOM.canvas ? DOM.canvas.getContext('2d') : null;

// ==========================================
// INITIALIZATION
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initTabs();
  initCanvas();
  bindEvents();
  fetchNetworkData();
});

// ==========================================
// API CALLS
// ==========================================
async function fetchNetworkData() {
  try {
    const res = await fetch('/api/network');
    if (!res.ok) throw new Error('Failed to fetch network state');
    const data = await res.json();

    AppState.users = data.users || [];
    AppState.friendships = data.friendships || [];
    AppState.stats = data.stats || {};

    updateUI();
  } catch (err) {
    console.error('Error fetching data:', err);
    showToast('Could not connect to Java server. Check if backend is running.', 'error');
  }
}

// ==========================================
// UI UPDATE DISPATCHER
// ==========================================
function updateUI() {
  updateStatsCards();
  updateDropdowns();
  renderDirectory(AppState.users);
  updateLeaderboard();
  updateStructuralMetrics();
  syncCanvasNodes();
}

function updateStatsCards() {
  DOM.statTotalUsers.textContent = AppState.stats.totalUsers || 0;
  DOM.statTotalFriendships.textContent = AppState.stats.totalFriendships || 0;
  DOM.statPopularUser.textContent = AppState.stats.mostPopularUser || '-';
  DOM.statPopularCount.textContent = (AppState.stats.maxFriends || 0) + ' connections';
  DOM.statAvgFriends.textContent = (AppState.stats.averageFriends || 0).toFixed(2);
  DOM.statIsolatedCount.textContent = (AppState.stats.isolatedUsersCount || 0) + ' isolated';
}

function updateDropdowns() {
  const users = AppState.users;
  const selectors = [
    DOM.selectFriend1,
    DOM.selectFriend2,
    DOM.selectMutual1,
    DOM.selectMutual2,
    DOM.selectSuggestionUser
  ];

  selectors.forEach(select => {
    if (!select) return;
    const currentVal = select.value;
    const placeholder = select.firstElementChild ? select.firstElementChild.outerHTML : '';
    
    let optionsHtml = placeholder;
    users.forEach(u => {
      const selected = u.name === currentVal ? 'selected' : '';
      optionsHtml += `<option value="${escapeHtml(u.name)}" ${selected}>${escapeHtml(u.name)} (${u.friendCount} friends)</option>`;
    });
    select.innerHTML = optionsHtml;
  });
}

function renderDirectory(usersToRender) {
  DOM.usersDirectoryGrid.innerHTML = '';
  DOM.directoryCountBadge.textContent = `Showing ${usersToRender.length} of ${AppState.users.length} users`;

  if (usersToRender.length === 0) {
    DOM.usersDirectoryGrid.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <p>No matching users found.</p>
      </div>
    `;
    return;
  }

  usersToRender.forEach(user => {
    const card = document.createElement('div');
    card.className = 'user-card';

    const avatarInitial = user.name ? user.name.charAt(0).toUpperCase() : '?';

    let friendsHtml = '';
    if (user.friends && user.friends.length > 0) {
      friendsHtml = user.friends
        .map(f => `<span class="friend-pill" onclick="openUserDetails('${escapeHtml(f)}')">${escapeHtml(f)}</span>`)
        .join('');
    } else {
      friendsHtml = '<span class="text-muted" style="font-size:0.75rem;">No friends added yet</span>';
    }

    card.innerHTML = `
      <div class="user-card-top">
        <div class="user-avatar">${avatarInitial}</div>
        <div class="user-details">
          <h4>${escapeHtml(user.name)}</h4>
          <span class="badge ${user.friendCount > 0 ? 'pill-badge' : ''}">${user.friendCount} Connections</span>
        </div>
      </div>
      <div class="user-friends-preview">
        ${friendsHtml}
      </div>
      <div class="user-card-actions">
        <button class="btn btn-outline btn-sm flex-1" onclick="openUserDetails('${escapeHtml(user.name)}')">
          View Profile
        </button>
        <button class="btn btn-primary btn-sm" onclick="quickSuggestFor('${escapeHtml(user.name)}')" title="Get Suggestions">
          ✨ Suggestions
        </button>
      </div>
    `;

    DOM.usersDirectoryGrid.appendChild(card);
  });
}

function updateLeaderboard() {
  DOM.leaderboardTbody.innerHTML = '';

  // Sort users descending by friend count (simple copy & sort)
  const sorted = [...AppState.users].sort((a, b) => b.friendCount - a.friendCount);

  if (sorted.length === 0) {
    DOM.leaderboardTbody.innerHTML = `<tr><td colspan="4" class="text-muted" style="text-align:center;">No users registered</td></tr>`;
    return;
  }

  const maxTotal = AppState.users.length > 1 ? (AppState.users.length - 1) : 1;

  sorted.forEach((u, idx) => {
    const rankClass = idx === 0 ? 'rank-1' : idx === 1 ? 'rank-2' : idx === 2 ? 'rank-3' : '';
    const sharePct = Math.round((u.friendCount / maxTotal) * 100);

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="rank-badge ${rankClass}">${idx + 1}</span></td>
      <td><strong>${escapeHtml(u.name)}</strong></td>
      <td><span class="badge pill-badge">${u.friendCount}</span></td>
      <td>
        <div style="display:flex; align-items:center; gap:0.5rem;">
          <div style="flex:1; height:6px; background:rgba(255,255,255,0.08); border-radius:3px; overflow:hidden;">
            <div style="width:${sharePct}%; height:100%; background:var(--accent-primary);"></div>
          </div>
          <span style="font-size:0.75rem; color:var(--text-secondary); width:32px;">${sharePct}%</span>
        </div>
      </td>
    `;
    DOM.leaderboardTbody.appendChild(tr);
  });
}

function updateStructuralMetrics() {
  const n = AppState.users.length;
  const maxPossibleEdges = n > 1 ? (n * (n - 1)) / 2 : 0;
  const actualEdges = AppState.stats.totalFriendships || 0;
  const density = maxPossibleEdges > 0 ? ((actualEdges / maxPossibleEdges) * 100).toFixed(1) : '0.0';

  DOM.metricDensity.textContent = density + '%';
  DOM.metricConnectedUsers.textContent = (n - (AppState.stats.isolatedUsersCount || 0));
  DOM.metricIsolatedUsers.textContent = AppState.stats.isolatedUsersCount || 0;
  DOM.metricMaxFriends.textContent = AppState.stats.maxFriends || 0;
  DOM.metricAvgDegree.textContent = (AppState.stats.averageFriends || 0).toFixed(2);
}

// ==========================================
// EVENT HANDLERS & FORMS
// ==========================================
function bindEvents() {
  // Add User Form
  DOM.formAddUser.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = DOM.inputUserName.value.trim();
    if (!name) return;

    try {
      const res = await fetch('/api/user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
      });
      const data = await res.json();
      if (data.success) {
        showToast(data.message, 'success');
        DOM.inputUserName.value = '';
        fetchNetworkData();
      } else {
        showToast(data.message, 'error');
      }
    } catch (err) {
      showToast('Error connecting to backend.', 'error');
    }
  });

  // Add Friendship Form
  DOM.formAddFriendship.addEventListener('submit', async (e) => {
    e.preventDefault();
    const u1 = DOM.selectFriend1.value;
    const u2 = DOM.selectFriend2.value;

    if (!u1 || !u2) {
      showToast('Please select two users.', 'error');
      return;
    }

    if (u1 === u2) {
      showToast('A user cannot befriend themselves!', 'error');
      return;
    }

    try {
      const res = await fetch('/api/friendship', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ user1: u1, user2: u2 })
      });
      const data = await res.json();
      if (data.success) {
        showToast(data.message, 'success');
        DOM.selectFriend1.value = '';
        DOM.selectFriend2.value = '';
        fetchNetworkData();
      } else {
        showToast(data.message, 'error');
      }
    } catch (err) {
      showToast('Error connecting to backend.', 'error');
    }
  });

  // Find Mutual Friends
  DOM.btnFindMutual.addEventListener('click', handleFindMutual);

  // Get Suggestions
  DOM.btnGetSuggestions.addEventListener('click', handleGetSuggestions);

  // Search User
  DOM.inputSearchUser.addEventListener('input', (e) => {
    const q = e.target.value.toLowerCase().trim();
    if (q) {
      DOM.btnClearSearch.classList.remove('hidden');
      const filtered = AppState.users.filter(u => u.name.toLowerCase().includes(q));
      renderDirectory(filtered);
    } else {
      DOM.btnClearSearch.classList.add('hidden');
      renderDirectory(AppState.users);
    }
  });

  DOM.btnClearSearch.addEventListener('click', () => {
    DOM.inputSearchUser.value = '';
    DOM.btnClearSearch.classList.add('hidden');
    renderDirectory(AppState.users);
  });

  // Demo Data Button
  DOM.btnDemo.addEventListener('click', async () => {
    try {
      const res = await fetch('/api/demo', { method: 'POST' });
      const data = await res.json();
      showToast(data.message, 'success');
      fetchNetworkData();
    } catch (err) {
      showToast('Error loading demo data.', 'error');
    }
  });

  // Reset Button
  DOM.btnReset.addEventListener('click', async () => {
    if (!confirm('Are you sure you want to clear all users and friendships?')) return;
    try {
      const res = await fetch('/api/reset', { method: 'POST' });
      const data = await res.json();
      showToast(data.message, 'info');
      fetchNetworkData();
    } catch (err) {
      showToast('Error resetting network.', 'error');
    }
  });

  // Viva Modal
  DOM.btnViva.addEventListener('click', () => {
    DOM.vivaModal.classList.remove('hidden');
  });
  DOM.vivaModalClose.addEventListener('click', () => {
    DOM.vivaModal.classList.add('hidden');
  });

  // User Profile Modal Close
  DOM.modalClose.addEventListener('click', () => {
    DOM.userModal.classList.add('hidden');
  });

  // Close modals on clicking outside
  window.addEventListener('click', (e) => {
    if (e.target === DOM.userModal) DOM.userModal.classList.add('hidden');
    if (e.target === DOM.vivaModal) DOM.vivaModal.classList.add('hidden');
  });

  // Theme Toggle
  DOM.themeToggle.addEventListener('click', toggleTheme);

  // Recenter Button
  DOM.btnRecenter.addEventListener('click', () => {
    syncCanvasNodes(true);
    showToast('Network visualization recentered', 'info');
  });
}

// ==========================================
// MUTUAL FRIENDS LOGIC (Feature D)
// ==========================================
async function handleFindMutual() {
  const u1 = DOM.selectMutual1.value;
  const u2 = DOM.selectMutual2.value;

  if (!u1 || !u2) {
    showToast('Please select both users to find mutual friends.', 'error');
    return;
  }

  if (u1 === u2) {
    showToast('Please select two distinct users.', 'error');
    return;
  }

  try {
    const res = await fetch(`/api/mutual?user1=${encodeURIComponent(u1)}&user2=${encodeURIComponent(u2)}`);
    const data = await res.json();

    const mutuals = data.mutualFriends || [];
    if (mutuals.length === 0) {
      DOM.mutualResultsContainer.innerHTML = `
        <div class="empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="8" y1="12" x2="16" y2="12"></line>
          </svg>
          <p><strong>${escapeHtml(u1)}</strong> and <strong>${escapeHtml(u2)}</strong> do not share any mutual friends.</p>
        </div>
      `;
    } else {
      let cardsHtml = `
        <div style="margin-bottom:1rem; display:flex; align-items:center; justify-content:space-between;">
          <h4 style="font-size:0.95rem;">Mutual Friends (${mutuals.length}) between <span class="text-accent">${escapeHtml(u1)}</span> and <span class="text-accent">${escapeHtml(u2)}</span>:</h4>
          <span class="badge pill-badge">${mutuals.length} Shared</span>
        </div>
        <div class="mutual-cards-grid">
      `;

      mutuals.forEach(name => {
        const initial = name ? name.charAt(0).toUpperCase() : '?';
        cardsHtml += `
          <div class="mutual-person-card">
            <div class="user-avatar" style="width:36px; height:36px; font-size:0.9rem;">${initial}</div>
            <div>
              <strong style="font-size:0.9rem;">${escapeHtml(name)}</strong>
              <div style="font-size:0.75rem; color:var(--text-secondary);">Mutual Friend</div>
            </div>
          </div>
        `;
      });

      cardsHtml += `</div>`;
      DOM.mutualResultsContainer.innerHTML = cardsHtml;
    }
  } catch (err) {
    showToast('Error finding mutual friends.', 'error');
  }
}

// ==========================================
// FRIEND SUGGESTIONS LOGIC (Feature E)
// ==========================================
async function handleGetSuggestions() {
  const user = DOM.selectSuggestionUser.value;
  if (!user) {
    showToast('Please select a user first.', 'error');
    return;
  }

  try {
    const res = await fetch(`/api/suggestions?user=${encodeURIComponent(user)}`);
    const data = await res.json();

    const suggestions = data.suggestions || [];
    if (suggestions.length === 0) {
      DOM.suggestionsResultsContainer.innerHTML = `
        <div class="empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <p>No new suggestions available for <strong>${escapeHtml(user)}</strong>.<br>
          <span style="font-size:0.8rem; color:var(--text-muted);">(Suggestions are friends-of-friends who are not yet connected directly)</span></p>
        </div>
      `;
    } else {
      let cardsHtml = `
        <div style="margin-bottom:1rem; display:flex; align-items:center; justify-content:space-between;">
          <h4 style="font-size:0.95rem;">Suggested Friends for <span class="text-accent">${escapeHtml(user)}</span>:</h4>
          <span class="badge pill-badge">${suggestions.length} Recommendations</span>
        </div>
        <div class="suggestions-grid">
      `;

      suggestions.forEach(s => {
        const initial = s.candidateName ? s.candidateName.charAt(0).toUpperCase() : '?';
        const viaList = s.mutualFriends ? s.mutualFriends.join(', ') : 'Mutual Friends';

        cardsHtml += `
          <div class="suggestion-card">
            <div class="suggestion-card-header">
              <div style="display:flex; align-items:center; gap:0.75rem;">
                <div class="user-avatar" style="width:40px; height:40px;">${initial}</div>
                <div>
                  <h4 style="font-size:0.95rem;">${escapeHtml(s.candidateName)}</h4>
                  <span class="badge" style="font-size:0.7rem;">${s.mutualCount} mutual connection(s)</span>
                </div>
              </div>
            </div>
            <div class="mutual-reason-text">
              🤝 Connected via: <strong>${escapeHtml(viaList)}</strong>
            </div>
            <button class="btn btn-primary btn-sm btn-block" onclick="connectSuggestion('${escapeHtml(user)}', '${escapeHtml(s.candidateName)}')">
              + Connect with ${escapeHtml(s.candidateName)}
            </button>
          </div>
        `;
      });

      cardsHtml += `</div>`;
      DOM.suggestionsResultsContainer.innerHTML = cardsHtml;
    }
  } catch (err) {
    showToast('Error getting friend suggestions.', 'error');
  }
}

async function connectSuggestion(u1, u2) {
  try {
    const res = await fetch('/api/friendship', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ user1: u1, user2: u2 })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      await fetchNetworkData();
      handleGetSuggestions(); // Refresh suggestions pane
    } else {
      showToast(data.message, 'error');
    }
  } catch (err) {
    showToast('Failed to connect users.', 'error');
  }
}

// Quick Suggestion Trigger from Directory or Modal
function quickSuggestFor(userName) {
  // Switch to suggestions tab
  switchTab('tab-suggestions');
  DOM.selectSuggestionUser.value = userName;
  handleGetSuggestions();
}

// ==========================================
// USER DETAILS MODAL
// ==========================================
function openUserDetails(userName) {
  const user = AppState.users.find(u => u.name.equalsIgnoreCase ? u.name.equalsIgnoreCase(userName) : u.name.toLowerCase() === userName.toLowerCase());
  if (!user) return;

  AppState.selectedUser = user;
  DOM.modalAvatar.textContent = user.name ? user.name.charAt(0).toUpperCase() : '?';
  DOM.modalUserName.textContent = user.name;
  DOM.modalUserConnections.textContent = `${user.friendCount} Direct Connection(s)`;
  DOM.modalFriendCount.textContent = user.friendCount;

  if (user.friends && user.friends.length > 0) {
    DOM.modalFriendsList.innerHTML = user.friends
      .map(f => `<span class="badge pill-badge" style="cursor:pointer;" onclick="openUserDetails('${escapeHtml(f)}')">${escapeHtml(f)}</span>`)
      .join('');
  } else {
    DOM.modalFriendsList.innerHTML = '<span class="text-muted" style="font-size:0.8rem;">No friends yet. Add connections above!</span>';
  }

  DOM.modalBtnFindMutual.onclick = () => {
    DOM.userModal.classList.add('hidden');
    switchTab('tab-mutual');
    DOM.selectMutual1.value = user.name;
  };

  DOM.modalBtnGetSuggestions.onclick = () => {
    DOM.userModal.classList.add('hidden');
    quickSuggestFor(user.name);
  };

  DOM.userModal.classList.remove('hidden');
}

// ==========================================
// TABS SWITCHER
// ==========================================
function initTabs() {
  DOM.tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const tabId = btn.getAttribute('data-tab');
      switchTab(tabId);
    });
  });
}

function switchTab(tabId) {
  DOM.tabBtns.forEach(b => b.classList.toggle('active', b.getAttribute('data-tab') === tabId));
  DOM.tabPanes.forEach(p => p.classList.toggle('active', p.id === tabId));
}

// ==========================================
// INTERACTIVE HTML5 CANVAS NETWORK VISUALIZER
// ==========================================
function initCanvas() {
  if (!DOM.canvas) return;

  function resize() {
    const rect = DOM.canvas.parentElement.getBoundingClientRect();
    DOM.canvas.width = rect.width;
    DOM.canvas.height = rect.height;
    renderCanvas();
  }

  window.addEventListener('resize', resize);
  resize();

  // Mouse / Touch Dragging
  DOM.canvas.addEventListener('mousedown', (e) => {
    const pos = getCanvasMousePos(e);
    const clickedNode = findNodeAt(pos.x, pos.y);
    if (clickedNode) {
      AppState.draggingNode = clickedNode;
      AppState.dragOffset = { x: pos.x - clickedNode.x, y: pos.y - clickedNode.y };
    }
  });

  DOM.canvas.addEventListener('mousemove', (e) => {
    const pos = getCanvasMousePos(e);

    if (AppState.draggingNode) {
      AppState.draggingNode.x = pos.x - AppState.dragOffset.x;
      AppState.draggingNode.y = pos.y - AppState.dragOffset.y;
      renderCanvas();
    } else {
      const node = findNodeAt(pos.x, pos.y);
      if (node !== AppState.hoveredNode) {
        AppState.hoveredNode = node;
        DOM.canvas.style.cursor = node ? 'pointer' : 'grab';
        renderCanvas();
      }
    }
  });

  DOM.canvas.addEventListener('mouseup', () => {
    AppState.draggingNode = null;
  });

  DOM.canvas.addEventListener('mouseleave', () => {
    AppState.draggingNode = null;
    AppState.hoveredNode = null;
    renderCanvas();
  });

  DOM.canvas.addEventListener('click', (e) => {
    const pos = getCanvasMousePos(e);
    const clicked = findNodeAt(pos.x, pos.y);
    if (clicked) {
      openUserDetails(clicked.name);
    }
  });
}

function getCanvasMousePos(evt) {
  const rect = DOM.canvas.getBoundingClientRect();
  return {
    x: evt.clientX - rect.left,
    y: evt.clientY - rect.top
  };
}

function findNodeAt(x, y) {
  for (let i = AppState.canvasNodes.length - 1; i >= 0; i--) {
    const node = AppState.canvasNodes[i];
    const dx = x - node.x;
    const dy = y - node.y;
    if (Math.sqrt(dx * dx + dy * dy) <= node.radius + 6) {
      return node;
    }
  }
  return null;
}

function syncCanvasNodes(forceRecenter = false) {
  if (!DOM.canvas) return;
  const w = DOM.canvas.width;
  const h = DOM.canvas.height;
  const centerX = w / 2;
  const centerY = h / 2;
  const count = AppState.users.length;

  if (count === 0) {
    AppState.canvasNodes = [];
    renderCanvas();
    return;
  }

  const existingMap = new Map();
  if (!forceRecenter) {
    AppState.canvasNodes.forEach(n => existingMap.set(n.name.toLowerCase(), { x: n.x, y: n.y }));
  }

  const radius = Math.min(w, h) * 0.35;
  const newNodes = [];

  AppState.users.forEach((u, i) => {
    const key = u.name.toLowerCase();
    let pos = existingMap.get(key);

    if (!pos || forceRecenter) {
      const angle = (2 * Math.PI * i) / count;
      pos = {
        x: centerX + radius * Math.cos(angle) + (Math.random() * 20 - 10),
        y: centerY + radius * Math.sin(angle) + (Math.random() * 20 - 10)
      };
    }

    const nodeRadius = 20 + Math.min(u.friendCount * 3, 14);

    newNodes.push({
      name: u.name,
      friendCount: u.friendCount,
      friends: u.friends || [],
      x: pos.x,
      y: pos.y,
      radius: nodeRadius
    });
  });

  AppState.canvasNodes = newNodes;
  renderCanvas();
}

function renderCanvas() {
  if (!ctx || !DOM.canvas) return;
  const w = DOM.canvas.width;
  const h = DOM.canvas.height;

  ctx.clearRect(0, 0, w, h);

  const isLight = document.body.classList.contains('theme-light');
  const nodeMap = new Map();
  AppState.canvasNodes.forEach(n => nodeMap.set(n.name.toLowerCase(), n));

  const hovered = AppState.hoveredNode;

  // 1. Draw Friendship Connection Lines
  AppState.friendships.forEach(edge => {
    const n1 = nodeMap.get(edge.source.toLowerCase());
    const n2 = nodeMap.get(edge.target.toLowerCase());

    if (!n1 || !n2) return;

    let isHighlighted = false;
    let isDimmed = false;

    if (hovered) {
      if ((n1 === hovered || n2 === hovered)) {
        isHighlighted = true;
      } else {
        isDimmed = true;
      }
    }

    ctx.beginPath();
    ctx.moveTo(n1.x, n1.y);
    ctx.lineTo(n2.x, n2.y);

    if (isHighlighted) {
      ctx.strokeStyle = '#6366f1';
      ctx.lineWidth = 3.5;
      ctx.shadowColor = '#6366f1';
      ctx.shadowBlur = 10;
    } else if (isDimmed) {
      ctx.strokeStyle = isLight ? 'rgba(0, 0, 0, 0.05)' : 'rgba(255, 255, 255, 0.03)';
      ctx.lineWidth = 1;
      ctx.shadowBlur = 0;
    } else {
      ctx.strokeStyle = isLight ? 'rgba(99, 102, 241, 0.35)' : 'rgba(99, 102, 241, 0.4)';
      ctx.lineWidth = 2;
      ctx.shadowBlur = 0;
    }

    ctx.stroke();
    ctx.shadowBlur = 0;
  });

  // 2. Draw Nodes
  AppState.canvasNodes.forEach(node => {
    const isHovered = hovered === node;
    const isNeighbor = hovered && hovered.friends.some(f => f.toLowerCase() === node.name.toLowerCase());
    const isDimmed = hovered && !isHovered && !isNeighbor;

    ctx.save();

    if (isDimmed) {
      ctx.globalAlpha = 0.3;
    }

    // Outer glow ring
    ctx.beginPath();
    ctx.arc(node.x, node.y, node.radius + (isHovered ? 6 : 2), 0, 2 * Math.PI);
    ctx.fillStyle = isHovered 
      ? 'rgba(99, 102, 241, 0.4)' 
      : (node.friendCount > 2 ? 'rgba(20, 184, 166, 0.25)' : 'rgba(255, 255, 255, 0.06)');
    ctx.fill();

    // Node body circle
    ctx.beginPath();
    ctx.arc(node.x, node.y, node.radius, 0, 2 * Math.PI);

    const grad = ctx.createLinearGradient(node.x - node.radius, node.y - node.radius, node.x + node.radius, node.y + node.radius);
    if (node.name === AppState.stats.mostPopularUser && node.friendCount > 0) {
      grad.addColorStop(0, '#f59e0b');
      grad.addColorStop(1, '#d97706');
    } else if (node.friendCount >= 2) {
      grad.addColorStop(0, '#6366f1');
      grad.addColorStop(1, '#8b5cf6');
    } else {
      grad.addColorStop(0, '#3b82f6');
      grad.addColorStop(1, '#14b8a6');
    }

    ctx.fillStyle = grad;
    ctx.fill();
    ctx.lineWidth = isHovered ? 3 : 1.5;
    ctx.strokeStyle = isHovered ? '#ffffff' : 'rgba(255, 255, 255, 0.3)';
    ctx.stroke();

    // Avatar Initial
    ctx.fillStyle = '#ffffff';
    ctx.font = `bold ${Math.round(node.radius * 0.75)}px Outfit, sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(node.name.charAt(0).toUpperCase(), node.x, node.y);

    // Name Label below node
    ctx.fillStyle = isLight ? '#111827' : '#f9fafb';
    ctx.font = '600 12px Inter, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(node.name, node.x, node.y + node.radius + 15);

    // Friend count indicator
    ctx.fillStyle = '#9ca3af';
    ctx.font = '500 10px Inter, sans-serif';
    ctx.fillText(`${node.friendCount} friend${node.friendCount === 1 ? '' : 's'}`, node.x, node.y + node.radius + 28);

    ctx.restore();
  });
}

// ==========================================
// TOAST NOTIFICATIONS
// ==========================================
function showToast(message, type = 'info') {
  if (!DOM.toastContainer) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
  DOM.toastContainer.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.25s ease';
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

// ==========================================
// THEME MANAGER
// ==========================================
function initTheme() {
  const saved = localStorage.getItem('sna_theme');
  if (saved === 'light') {
    document.body.classList.remove('theme-dark');
    document.body.classList.add('theme-light');
    updateThemeIcons(true);
  } else {
    document.body.classList.remove('theme-light');
    document.body.classList.add('theme-dark');
    updateThemeIcons(false);
  }
}

function toggleTheme() {
  const isLight = document.body.classList.toggle('theme-light');
  document.body.classList.toggle('theme-dark', !isLight);
  localStorage.setItem('sna_theme', isLight ? 'light' : 'dark');
  updateThemeIcons(isLight);
  renderCanvas();
}

function updateThemeIcons(isLight) {
  const sun = document.getElementById('theme-icon-sun');
  const moon = document.getElementById('theme-icon-moon');
  if (sun && moon) {
    sun.classList.toggle('hidden', isLight);
    moon.classList.toggle('hidden', !isLight);
  }
}

// ==========================================
// UTILITY HELPERS
// ==========================================
function escapeHtml(text) {
  if (!text) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Global exposes for inline HTML onclick handlers
window.openUserDetails = openUserDetails;
window.quickSuggestFor = quickSuggestFor;
window.connectSuggestion = connectSuggestion;
