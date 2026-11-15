/**
 * EcoCampus – Energy Reading Analyzer
 * Frontend Dashboard Controller & Java Backend Integration Client
 */

// Application State
const AppState = {
    tariffRate: 8.50,
    anomalyThreshold: 100.0,
    selectedDate: new Date().toISOString().split('T')[0],
    overview: null,
    meters: [],
    buildings: [],
    readings: [],
    comparison: null,
    anomalies: null,
    repeatedAlerts: null,
    bill: null,
    charts: {}
};

// DOM Elements
const Elements = {
    // Nav items
    navItems: document.querySelectorAll('.nav-item'),
    sections: document.querySelectorAll('.section-view'),
    pageTitle: document.getElementById('page-title'),
    pageSubtitle: document.getElementById('page-subtitle'),
    backendPulse: document.getElementById('backend-pulse'),
    backendStatusText: document.getElementById('backend-status-text'),
    headerTariffDisplay: document.getElementById('header-tariff-display'),
    btnManualRefresh: document.getElementById('btn-manual-refresh'),

    // Dashboard Overview
    cardTotalEnergy: document.getElementById('card-total-energy'),
    cardEstimatedBill: document.getElementById('card-estimated-bill'),
    cardTariffRate: document.getElementById('card-tariff-rate'),
    cardBuildingCount: document.getElementById('card-building-count'),
    cardRoomApplianceSub: document.getElementById('card-room-appliance-sub'),
    cardMeterCount: document.getElementById('card-meter-count'),
    cardReadingCount: document.getElementById('card-reading-count'),
    cardAnomalyCount: document.getElementById('card-anomaly-count'),
    cardAnomalyThreshold: document.getElementById('card-anomaly-threshold'),
    cardRepeatedAlerts: document.getElementById('card-repeated-alerts'),
    dashboardAlertBanner: document.getElementById('dashboard-alert-banner'),
    dashboardAlertMsg: document.getElementById('dashboard-alert-msg'),
    sidebarAlertBadge: document.getElementById('sidebar-alert-badge'),

    overviewDayBadge: document.getElementById('overview-day-badge'),
    overviewDayCurrLabel: document.getElementById('overview-day-curr-label'),
    overviewDayCurrVal: document.getElementById('overview-day-curr-val'),
    overviewDayPrevLabel: document.getElementById('overview-day-prev-label'),
    overviewDayPrevVal: document.getElementById('overview-day-prev-val'),

    overviewWeekBadge: document.getElementById('overview-week-badge'),
    overviewWeekCurrLabel: document.getElementById('overview-week-curr-label'),
    overviewWeekCurrVal: document.getElementById('overview-week-curr-val'),
    overviewWeekPrevLabel: document.getElementById('overview-week-prev-label'),
    overviewWeekPrevVal: document.getElementById('overview-week-prev-val'),

    overviewLatestReadingsBody: document.getElementById('overview-latest-readings-body'),

    // Analytics
    analyticsRefDate: document.getElementById('analytics-ref-date'),
    btnApplyDateFilter: document.getElementById('btn-apply-date-filter'),
    analyticsDayPeriodText: document.getElementById('analytics-day-period-text'),
    analyticsDayBadge: document.getElementById('analytics-day-badge'),
    analyticsDayCurrKwh: document.getElementById('analytics-day-curr-kwh'),
    analyticsDayPrevKwh: document.getElementById('analytics-day-prev-kwh'),
    analyticsDaySummaryText: document.getElementById('analytics-day-summary-text'),

    analyticsWeekPeriodText: document.getElementById('analytics-week-period-text'),
    analyticsWeekBadge: document.getElementById('analytics-week-badge'),
    analyticsWeekCurrKwh: document.getElementById('analytics-week-curr-kwh'),
    analyticsWeekPrevKwh: document.getElementById('analytics-week-prev-kwh'),
    analyticsWeekSummaryText: document.getElementById('analytics-week-summary-text'),

    // Buildings
    buildingsCardsContainer: document.getElementById('buildings-cards-container'),
    buildingsMetersTableBody: document.getElementById('buildings-meters-table-body'),

    // Anomalies
    anomalyThresholdInput: document.getElementById('anomaly-threshold-input'),
    btnApplyAnomalyThresh: document.getElementById('btn-apply-anomaly-thresh'),
    repeatedAlertsBadge: document.getElementById('repeated-alerts-badge'),
    repeatedAlertsContainer: document.getElementById('repeated-alerts-container'),
    anomaliesCountBadge: document.getElementById('anomalies-count-badge'),
    anomaliesTableBody: document.getElementById('anomalies-table-body'),

    // Billing
    billTotalAmount: document.getElementById('bill-total-amount'),
    billHeroKwh: document.getElementById('bill-hero-kwh'),
    billHeroRate: document.getElementById('bill-hero-rate'),
    billTariffInput: document.getElementById('bill-tariff-input'),
    btnRecalculateBill: document.getElementById('btn-recalculate-bill'),
    billDisclaimerText: document.getElementById('bill-disclaimer-text'),
    billMeterBreakdownBody: document.getElementById('bill-meter-breakdown-body'),

    // Readings & Simulation
    formAddReading: document.getElementById('form-add-reading'),
    inputReadingId: document.getElementById('input-reading-id'),
    selectMeterId: document.getElementById('select-meter-id'),
    inputEnergyVal: document.getElementById('input-energy-val'),
    inputTimestamp: document.getElementById('input-timestamp'),
    btnRunConcurrent: document.getElementById('btn-run-concurrent'),
    concurrentResultBox: document.getElementById('concurrent-result-box'),
    btnLoadHistorical: document.getElementById('btn-load-historical'),
    historicalResultBox: document.getElementById('historical-result-box'),
    fullReadingsTableBody: document.getElementById('full-readings-table-body'),
    filterReadingsInput: document.getElementById('filter-readings-input'),

    // Automatic Live Monitoring
    btnStartMonitoring: document.getElementById('btn-start-monitoring'),
    btnStopMonitoring: document.getElementById('btn-stop-monitoring'),
    autoMonitorBadge: document.getElementById('auto-monitor-badge'),
    autoMonitorPulse: document.getElementById('auto-monitor-pulse'),
    autoMonitorStatusText: document.getElementById('auto-monitor-status-text'),
    autoMonitorCount: document.getElementById('auto-monitor-count'),
    autoMonitorDetails: document.getElementById('auto-monitor-details'),
    headerMonitoringPill: document.getElementById('header-monitoring-pill'),
    headerMonitoringText: document.getElementById('header-monitoring-text'),

    // Toast Container
    toastContainer: document.getElementById('toast-container')
};

// =============================================================================
// INITIALIZATION
// =============================================================================

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initControls();
    initTimestampField();

    if (Elements.analyticsRefDate) {
        Elements.analyticsRefDate.value = AppState.selectedDate;
    }

    // Load initial data from Java backend
    loadAllData();
});

function initNavigation() {
    Elements.navItems.forEach(item => {
        item.addEventListener('click', () => {
            const sectionId = item.getAttribute('data-section');
            navigateToSection(sectionId);
        });
    });
}

function navigateToSection(sectionId) {
    Elements.navItems.forEach(nav => {
        if (nav.getAttribute('data-section') === sectionId) {
            nav.classList.add('active');
        } else {
            nav.classList.remove('active');
        }
    });

    Elements.sections.forEach(sec => {
        if (sec.id === `section-${sectionId}`) {
            sec.classList.add('active');
        } else {
            sec.classList.remove('active');
        }
    });

    // Update Header titles
    const titles = {
        'dashboard': { title: 'Dashboard Overview', sub: 'Real-time electricity consumption & campus infrastructure metrics' },
        'analytics': { title: 'Energy Usage Analytics', sub: 'Day-over-day & week-over-week trends calculated with Java Time API' },
        'buildings': { title: 'Building Management & Asset Hierarchy', sub: 'Physical campus domain modeling (Building &rarr; Room &rarr; Appliance &rarr; Meter)' },
        'anomalies': { title: 'Anomaly Detection & Alert Center', sub: 'Polymorphic Strategy Pattern (HighUsageDetector & NegativeReadingDetector)' },
        'billing': { title: 'Electricity Bill Estimation', sub: 'Financial projection in Indian Rupees (₹) with per-meter breakdown' },
        'simulation': { title: 'Readings & Simulation Lab', sub: 'Multi-threaded worker ingestion and concurrent simulation testing' }
    };

    if (titles[sectionId]) {
        Elements.pageTitle.innerHTML = titles[sectionId].title;
        Elements.pageSubtitle.innerHTML = titles[sectionId].sub;
    }

    // Trigger chart resize if navigating to analytics or dashboard
    setTimeout(() => {
        Object.values(AppState.charts).forEach(c => {
            if (c) c.resize();
        });
    }, 100);
}

function initControls() {
    if (Elements.btnManualRefresh) {
        Elements.btnManualRefresh.addEventListener('click', () => {
            showToast('Refreshing Data', 'Fetching latest metrics from Java backend...', 'info');
            loadAllData();
        });
    }

    if (Elements.btnApplyDateFilter) {
        Elements.btnApplyDateFilter.addEventListener('click', () => {
            AppState.selectedDate = Elements.analyticsRefDate.value;
            loadComparisonData();
        });
    }

    if (Elements.btnApplyAnomalyThresh) {
        Elements.btnApplyAnomalyThresh.addEventListener('click', () => {
            const val = parseFloat(Elements.anomalyThresholdInput.value);
            if (!isNaN(val) && val > 0) {
                AppState.anomalyThreshold = val;
                loadAnomalyData();
                showToast('Threshold Updated', `High usage detector threshold set to ${val} kWh`, 'info');
            } else {
                showToast('Invalid Input', 'Please enter a valid positive number for threshold.', 'error');
            }
        });
    }

    if (Elements.btnRecalculateBill) {
        Elements.btnRecalculateBill.addEventListener('click', () => {
            const rate = parseFloat(Elements.billTariffInput.value);
            if (!isNaN(rate) && rate > 0) {
                AppState.tariffRate = rate;
                loadBillingData();
                showToast('Tariff Updated', `Recalculating estimated bill at ₹${rate.toFixed(2)} / unit`, 'info');
            } else {
                showToast('Invalid Tariff', 'Please enter a valid tariff rate in ₹/unit.', 'error');
            }
        });
    }

    // Form Add Reading
    if (Elements.formAddReading) {
        Elements.formAddReading.addEventListener('submit', handleAddReadingSubmit);
    }

    // Simulation buttons
    if (Elements.btnStartMonitoring) {
        Elements.btnStartMonitoring.addEventListener('click', handleStartMonitoring);
    }

    if (Elements.btnStopMonitoring) {
        Elements.btnStopMonitoring.addEventListener('click', handleStopMonitoring);
    }

    if (Elements.btnRunConcurrent) {
        Elements.btnRunConcurrent.addEventListener('click', handleRunConcurrentSimulation);
    }

    if (Elements.btnLoadHistorical) {
        Elements.btnLoadHistorical.addEventListener('click', handleLoadHistoricalSimulation);
    }

    // Filter readings input
    if (Elements.filterReadingsInput) {
        Elements.filterReadingsInput.addEventListener('input', () => {
            const query = Elements.filterReadingsInput.value.toLowerCase().trim();
            renderReadingsTable(AppState.readings, query);
        });
    }
}

function initTimestampField() {
    if (Elements.inputTimestamp) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        Elements.inputTimestamp.value = now.toISOString().slice(0, 16);
    }
}

// =============================================================================
// API DATA FETCHING
// =============================================================================

async function loadAllData() {
    try {
        setBackendStatus(true, 'Connecting...');
        await Promise.all([
            loadOverviewData(),
            loadMetersData(),
            loadBuildingsData(),
            loadReadingsData(),
            loadComparisonData(),
            loadAnomalyData(),
            loadBillingData()
        ]);
        setBackendStatus(true, 'Connected (Port 8080)');
    } catch (err) {
        console.error('Failed to load all data from backend:', err);
        setBackendStatus(false, 'Disconnected');
        showToast('Backend Error', 'Could not communicate with Java server on port 8080. Ensure WebServer is running.', 'error');
    }
}

function setBackendStatus(online, text) {
    if (Elements.backendPulse) {
        Elements.backendPulse.style.backgroundColor = online ? 'var(--primary-400)' : 'var(--danger)';
    }
    if (Elements.backendStatusText) {
        Elements.backendStatusText.textContent = `Backend: ${text}`;
    }
}

async function loadOverviewData() {
    const res = await fetch('/api/overview');
    if (!res.ok) throw new Error('Overview HTTP ' + res.status);
    const data = await res.json();
    AppState.overview = data;
    renderOverviewCardMetrics(data);
}

async function loadMetersData() {
    const res = await fetch('/api/meters');
    if (!res.ok) throw new Error('Meters HTTP ' + res.status);
    const data = await res.json();
    AppState.meters = data;
    renderBuildingsMetersTable(data);
}

async function loadBuildingsData() {
    const res = await fetch(`/api/buildings?rate=${AppState.tariffRate}`);
    if (!res.ok) throw new Error('Buildings HTTP ' + res.status);
    const data = await res.json();
    AppState.buildings = data;
    renderBuildingCards(data);
    renderBuildingOverviewChart(data);
}

async function loadReadingsData() {
    const res = await fetch('/api/readings');
    if (!res.ok) throw new Error('Readings HTTP ' + res.status);
    const data = await res.json();
    AppState.readings = data;
    renderOverviewLatestReadings(data.slice(0, 5));
    renderReadingsTable(data, Elements.filterReadingsInput ? Elements.filterReadingsInput.value : '');
    if (Elements.cardReadingCount) {
        Elements.cardReadingCount.textContent = `${data.length} reading${data.length === 1 ? '' : 's'} recorded`;
    }
}

async function loadComparisonData() {
    const res = await fetch(`/api/comparison?date=${AppState.selectedDate}`);
    if (!res.ok) throw new Error('Comparison HTTP ' + res.status);
    const data = await res.json();
    AppState.comparison = data;
    renderComparisonSection(data);
    renderTrendCharts(data);
}

async function loadAnomalyData() {
    const [resAnomalies, resRepeated] = await Promise.all([
        fetch(`/api/anomalies?threshold=${AppState.anomalyThreshold}`),
        fetch(`/api/repeated-alerts?threshold=${AppState.anomalyThreshold}&minViolations=2`)
    ]);

    if (!resAnomalies.ok || !resRepeated.ok) throw new Error('Anomaly HTTP error');
    const anomaliesData = await resAnomalies.json();
    const repeatedData = await resRepeated.json();

    AppState.anomalies = anomaliesData;
    AppState.repeatedAlerts = repeatedData;

    renderAnomaliesSection(anomaliesData, repeatedData);
}

async function loadBillingData() {
    const res = await fetch(`/api/bill?rate=${AppState.tariffRate}`);
    if (!res.ok) throw new Error('Bill HTTP ' + res.status);
    const data = await res.json();
    AppState.bill = data;
    renderBillingSection(data);
}

// =============================================================================
// RENDERING FUNCTIONS
// =============================================================================

function renderOverviewCardMetrics(overview) {
    if (Elements.cardTotalEnergy) {
        Elements.cardTotalEnergy.innerHTML = `${overview.totalEnergy.toFixed(2)} <small style="font-size: 14px; font-weight: normal;">kWh</small>`;
    }
    if (Elements.cardEstimatedBill) {
        Elements.cardEstimatedBill.textContent = `₹ ${overview.estimatedBill.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
    if (Elements.cardTariffRate) {
        Elements.cardTariffRate.textContent = `Tariff: ₹${overview.defaultTariff.toFixed(2)} / unit`;
    }
    if (Elements.headerTariffDisplay) {
        Elements.headerTariffDisplay.textContent = `₹${AppState.tariffRate.toFixed(2)} / kWh`;
    }
    if (Elements.cardMeterCount) {
        Elements.cardMeterCount.textContent = overview.activeMeters;
    }
    if (Elements.cardAnomalyCount) {
        Elements.cardAnomalyCount.textContent = overview.anomalyCount;
    }
    if (Elements.cardRepeatedAlerts) {
        Elements.cardRepeatedAlerts.textContent = overview.repeatedAlertCount;
    }
    if (Elements.sidebarAlertBadge) {
        if (overview.anomalyCount > 0 || overview.repeatedAlertCount > 0) {
            Elements.sidebarAlertBadge.style.display = 'inline-block';
            Elements.sidebarAlertBadge.textContent = overview.repeatedAlertCount > 0 ? overview.repeatedAlertCount : overview.anomalyCount;
        } else {
            Elements.sidebarAlertBadge.style.display = 'none';
        }
    }

    // Repeated alert banner on dashboard
    if (Elements.dashboardAlertBanner) {
        if (overview.repeatedAlertCount > 0) {
            Elements.dashboardAlertBanner.style.display = 'flex';
            Elements.dashboardAlertMsg.textContent = `${overview.repeatedAlertCount} meter(s) currently flag repeated high-usage violations exceeding ${AppState.anomalyThreshold} kWh. Facilities check recommended.`;
        } else {
            Elements.dashboardAlertBanner.style.display = 'none';
        }
    }
}

function renderComparisonSection(comp) {
    // Overview quick banners
    if (Elements.overviewDayCurrLabel) Elements.overviewDayCurrLabel.textContent = `Today (${comp.referenceDate})`;
    if (Elements.overviewDayCurrVal) Elements.overviewDayCurrVal.textContent = `${comp.currentDayKwh.toFixed(2)} kWh`;
    if (Elements.overviewDayPrevLabel) Elements.overviewDayPrevLabel.textContent = `Yesterday (${comp.previousDay})`;
    if (Elements.overviewDayPrevVal) Elements.overviewDayPrevVal.textContent = `${comp.previousDayKwh.toFixed(2)} kWh`;
    applyDiffBadge(Elements.overviewDayBadge, comp.dayPctChange, comp.dayDiff);

    if (Elements.overviewWeekCurrLabel) Elements.overviewWeekCurrLabel.textContent = `Current Week (${comp.currWeekStart} - ${comp.currWeekEnd})`;
    if (Elements.overviewWeekCurrVal) Elements.overviewWeekCurrVal.textContent = `${comp.currentWeekKwh.toFixed(2)} kWh`;
    if (Elements.overviewWeekPrevLabel) Elements.overviewWeekPrevLabel.textContent = `Prev Week (${comp.prevWeekStart} - ${comp.prevWeekEnd})`;
    if (Elements.overviewWeekPrevVal) Elements.overviewWeekPrevVal.textContent = `${comp.previousWeekKwh.toFixed(2)} kWh`;
    applyDiffBadge(Elements.overviewWeekBadge, comp.weekPctChange, comp.weekDiff);

    // Analytics section detailed cards
    if (Elements.analyticsDayPeriodText) {
        Elements.analyticsDayPeriodText.textContent = `${comp.referenceDate} vs ${comp.previousDay}`;
    }
    if (Elements.analyticsDayCurrKwh) Elements.analyticsDayCurrKwh.textContent = `${comp.currentDayKwh.toFixed(2)} kWh`;
    if (Elements.analyticsDayPrevKwh) Elements.analyticsDayPrevKwh.textContent = `${comp.previousDayKwh.toFixed(2)} kWh`;
    if (Elements.analyticsDaySummaryText) {
        const sign = comp.dayDiff > 0 ? '+' : '';
        Elements.analyticsDaySummaryText.innerHTML = `Net Difference: <strong>${sign}${comp.dayDiff.toFixed(2)} kWh</strong> &bull; Java Logic Analysis: <em>${escapeHtml(comp.dayPctChange)}</em>`;
    }
    applyDiffBadge(Elements.analyticsDayBadge, comp.dayPctChange, comp.dayDiff);

    if (Elements.analyticsWeekPeriodText) {
        Elements.analyticsWeekPeriodText.textContent = `Week of ${comp.currWeekStart} vs Week of ${comp.prevWeekStart}`;
    }
    if (Elements.analyticsWeekCurrKwh) Elements.analyticsWeekCurrKwh.textContent = `${comp.currentWeekKwh.toFixed(2)} kWh`;
    if (Elements.analyticsWeekPrevKwh) Elements.analyticsWeekPrevKwh.textContent = `${comp.previousWeekKwh.toFixed(2)} kWh`;
    if (Elements.analyticsWeekSummaryText) {
        const sign = comp.weekDiff > 0 ? '+' : '';
        Elements.analyticsWeekSummaryText.innerHTML = `Net Difference: <strong>${sign}${comp.weekDiff.toFixed(2)} kWh</strong> &bull; Java Logic Analysis: <em>${escapeHtml(comp.weekPctChange)}</em>`;
    }
    applyDiffBadge(Elements.analyticsWeekBadge, comp.weekPctChange, comp.weekDiff);
}

function applyDiffBadge(element, pctChangeStr, diff) {
    if (!element) return;
    element.className = 'diff-badge';
    element.textContent = pctChangeStr;

    if (diff > 0) {
        element.classList.add('increase');
        element.innerHTML = `▲ ${escapeHtml(pctChangeStr)}`;
    } else if (diff < 0) {
        element.classList.add('decrease');
        element.innerHTML = `▼ ${escapeHtml(pctChangeStr)}`;
    } else {
        element.classList.add('neutral');
        element.innerHTML = `● ${escapeHtml(pctChangeStr)}`;
    }
}

function renderOverviewLatestReadings(readings) {
    if (!Elements.overviewLatestReadingsBody) return;
    if (readings.length === 0) {
        Elements.overviewLatestReadingsBody.innerHTML = `<tr><td colspan="6" class="empty-state">No readings recorded yet. Use the simulation lab or add reading form.</td></tr>`;
        return;
    }

    let html = '';
    readings.forEach(r => {
        const meterInfo = getMeterInfo(r.meterId);
        const sourceBadge = r.isSimulated
            ? `<span class="badge badge-simulated">Simulated</span>`
            : `<span class="badge badge-live">Live</span>`;

        html += `
            <tr>
                <td><strong>${escapeHtml(r.readingId)}</strong></td>
                <td><code>${escapeHtml(r.meterId)}</code></td>
                <td>${escapeHtml(meterInfo.building)} &bull; <small style="color:var(--text-muted)">${escapeHtml(meterInfo.room)}</small></td>
                <td><strong>${r.energyConsumed.toFixed(2)} kWh</strong></td>
                <td>${formatTimestamp(r.timestamp)}</td>
                <td>${sourceBadge}</td>
            </tr>
        `;
    });
    Elements.overviewLatestReadingsBody.innerHTML = html;
}

function renderBuildingCards(buildings) {
    if (!Elements.buildingsCardsContainer) return;
    if (buildings.length === 0) {
        Elements.buildingsCardsContainer.innerHTML = `<div class="empty-state">No campus buildings returned by backend.</div>`;
        return;
    }

    if (Elements.cardBuildingCount) {
        Elements.cardBuildingCount.textContent = buildings.length;
    }

    let html = '';
    buildings.forEach(b => {
        html += `
            <div class="building-card">
                <div>
                    <div class="building-card-header">
                        <div class="building-card-title">
                            <h4>${escapeHtml(b.buildingName)}</h4>
                            <span>Asset ID: <code>${escapeHtml(b.buildingId)}</code></span>
                        </div>
                        <span class="badge badge-live">${b.sharePct.toFixed(1)}% Share</span>
                    </div>

                    <div class="building-metrics-row">
                        <div class="building-metric-item">
                            <div class="val">${b.totalKwh.toFixed(1)}</div>
                            <div class="lbl">Total kWh</div>
                        </div>
                        <div class="building-metric-item">
                            <div class="val">₹${b.estimatedCost.toFixed(0)}</div>
                            <div class="lbl">Est. Bill</div>
                        </div>
                        <div class="building-metric-item">
                            <div class="val">${b.readingCount}</div>
                            <div class="lbl">Readings</div>
                        </div>
                    </div>

                    <div style="font-size: 12px; margin-top: 10px;">
                        <strong style="color: var(--text-secondary);">Monitored Rooms & Facilities:</strong>
                        <div class="hierarchy-tags">
                            ${b.rooms.map(r => `<span class="hierarchy-tag">🚪 ${escapeHtml(r)}</span>`).join('')}
                        </div>
                    </div>

                    <div style="font-size: 12px; margin-top: 10px;">
                        <strong style="color: var(--text-secondary);">Installed Appliances:</strong>
                        <div class="hierarchy-tags">
                            ${b.appliances.map(a => `<span class="hierarchy-tag">⚡ ${escapeHtml(a)}</span>`).join('')}
                        </div>
                    </div>
                </div>

                <div style="margin-top: 16px; pt: 12px; border-top: 1px solid var(--border-subtle); display: flex; justify-content: space-between; align-items: center; font-size: 11px; color: var(--text-muted);">
                    <span>Assigned Meters: ${b.meters.map(m => `<code>${escapeHtml(m)}</code>`).join(', ')}</span>
                </div>
            </div>
        `;
    });
    Elements.buildingsCardsContainer.innerHTML = html;
}

function renderBuildingsMetersTable(meters) {
    if (!Elements.buildingsMetersTableBody) return;
    if (meters.length === 0) {
        Elements.buildingsMetersTableBody.innerHTML = `<tr><td colspan="8" class="empty-state">No meters registered.</td></tr>`;
        return;
    }

    let html = '';
    meters.forEach(m => {
        html += `
            <tr>
                <td><strong><code>${escapeHtml(m.meterId)}</code></strong></td>
                <td>${escapeHtml(m.building)}</td>
                <td>${escapeHtml(m.room)}</td>
                <td>${escapeHtml(m.appliance)}</td>
                <td>${m.readingCount}</td>
                <td><strong>${m.totalKwh.toFixed(2)} kWh</strong></td>
                <td>${m.sharePct.toFixed(1)}%</td>
                <td><strong>₹ ${m.estimatedCost.toFixed(2)}</strong></td>
            </tr>
        `;
    });
    Elements.buildingsMetersTableBody.innerHTML = html;
}

function renderAnomaliesSection(anomaliesData, repeatedData) {
    // Anomalies table
    if (Elements.anomaliesCountBadge) {
        Elements.anomaliesCountBadge.textContent = `${anomaliesData.totalCount} Anomalies`;
    }

    if (Elements.anomaliesTableBody) {
        if (!anomaliesData.anomalies || anomaliesData.anomalies.length === 0) {
            Elements.anomaliesTableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        <div class="empty-state-icon">✅</div>
                        <h4>Clean Energy Profile</h4>
                        <p>No anomalous readings found with current threshold (&gt; ${anomaliesData.threshold.toFixed(1)} kWh).</p>
                    </td>
                </tr>
            `;
        } else {
            let html = '';
            anomaliesData.anomalies.forEach(a => {
                const isNegative = a.energyConsumed < 0;
                const badge = isNegative
                    ? `<span class="badge badge-anomaly-neg">Negative Value</span>`
                    : `<span class="badge badge-anomaly-high">High Spikes</span>`;

                html += `
                    <tr>
                        <td><strong>${escapeHtml(a.readingId)}</strong></td>
                        <td><code>${escapeHtml(a.meterId)}</code></td>
                        <td><strong style="color: ${isNegative ? 'var(--warning)' : 'var(--danger)'};">${a.energyConsumed.toFixed(2)} kWh</strong></td>
                        <td>${formatTimestamp(a.timestamp)}</td>
                        <td><em>${escapeHtml(a.strategy)}</em></td>
                        <td>${badge}</td>
                    </tr>
                `;
            });
            Elements.anomaliesTableBody.innerHTML = html;
        }
    }

    // Repeated alerts
    if (Elements.repeatedAlertsBadge) {
        Elements.repeatedAlertsBadge.textContent = `${repeatedData.offendingMetersCount} Offending Meter${repeatedData.offendingMetersCount === 1 ? '' : 's'}`;
    }

    if (Elements.repeatedAlertsContainer) {
        if (!repeatedData.alerts || repeatedData.alerts.length === 0) {
            Elements.repeatedAlertsContainer.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🛡️</div>
                    <h4>No Persistent Energy Spikes</h4>
                    <p>No smart meter has exceeded the ${repeatedData.threshold.toFixed(1)} kWh threshold at least ${repeatedData.minViolations} times.</p>
                </div>
            `;
        } else {
            let html = '';
            repeatedData.alerts.forEach(alert => {
                const meterInfo = getMeterInfo(alert.meterId);
                html += `
                    <div style="background-color: var(--danger-soft); border: 1px solid #fecaca; border-radius: var(--radius-md); padding: 18px; margin-bottom: 14px;">
                        <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px;">
                            <div style="display: flex; align-items: center; gap: 10px;">
                                <span style="font-size: 20px;">🚨</span>
                                <div>
                                    <h4 style="color: #991b1b; font-size: 15px; font-weight: 700;">Repeated Violation: Meter ${escapeHtml(alert.meterId)}</h4>
                                    <span style="font-size: 12px; color: #b91c1c;">Location: ${escapeHtml(meterInfo.building)} &bull; ${escapeHtml(meterInfo.room)} (${escapeHtml(meterInfo.appliance)})</span>
                                </div>
                            </div>
                            <span class="badge" style="background-color: #991b1b; color: #ffffff; font-weight: 700;">${alert.violationCount} Violations Recorded</span>
                        </div>
                        <div style="font-size: 12px; color: #7f1d1d; margin-bottom: 8px;">
                            The following readings generated by this meter exceeded the safety threshold of ${repeatedData.threshold.toFixed(1)} kWh:
                        </div>
                        <div style="background-color: #ffffff; border-radius: var(--radius-sm); border: 1px solid #fca5a5; padding: 8px 12px;">
                            <table style="width: 100%; font-size: 12px; border-collapse: collapse;">
                                <thead>
                                    <tr style="text-align: left; color: var(--text-muted); border-bottom: 1px solid #fee2e2;">
                                        <th style="padding: 4px;">Reading ID</th>
                                        <th style="padding: 4px;">Timestamp</th>
                                        <th style="padding: 4px;">Consumption</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${alert.violations.map(v => `
                                        <tr>
                                            <td style="padding: 4px;"><code>${escapeHtml(v.readingId)}</code></td>
                                            <td style="padding: 4px;">${formatTimestamp(v.timestamp)}</td>
                                            <td style="padding: 4px; color: #991b1b; font-weight: 700;">${v.energyConsumed.toFixed(2)} kWh</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>
                `;
            });
            Elements.repeatedAlertsContainer.innerHTML = html;
        }
    }
}

function renderBillingSection(bill) {
    if (Elements.billTotalAmount) {
        Elements.billTotalAmount.textContent = `₹ ${bill.totalEstimatedBill.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    }
    if (Elements.billHeroKwh) {
        Elements.billHeroKwh.textContent = `${bill.totalEnergyKwh.toFixed(2)} kWh`;
    }
    if (Elements.billHeroRate) {
        Elements.billHeroRate.textContent = `₹${bill.ratePerUnit.toFixed(2)} / kWh`;
    }
    if (Elements.billDisclaimerText && bill.disclaimer) {
        Elements.billDisclaimerText.textContent = bill.disclaimer;
    }

    if (Elements.billMeterBreakdownBody) {
        if (!bill.perMeterBreakdown || bill.perMeterBreakdown.length === 0) {
            Elements.billMeterBreakdownBody.innerHTML = `<tr><td colspan="6" class="empty-state">No meter readings recorded. Total bill is ₹0.00.</td></tr>`;
            return;
        }

        let html = '';
        bill.perMeterBreakdown.forEach(item => {
            const meterInfo = getMeterInfo(item.meterId);
            const share = bill.totalEnergyKwh > 0 ? (item.kwh / bill.totalEnergyKwh) * 100.0 : 0;
            html += `
                <tr>
                    <td><strong><code>${escapeHtml(item.meterId)}</code></strong></td>
                    <td>${escapeHtml(meterInfo.building)}</td>
                    <td>${escapeHtml(meterInfo.appliance)}</td>
                    <td>${item.kwh.toFixed(2)} kWh</td>
                    <td><strong style="color: var(--primary-700);">₹ ${item.estimatedCost.toFixed(2)}</strong></td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <div style="flex: 1; background: #e2e8f0; height: 6px; border-radius: 3px; overflow: hidden;">
                                <div style="background: var(--primary-500); width: ${share.toFixed(1)}%; height: 100%;"></div>
                            </div>
                            <span style="font-size: 11px; width: 38px;">${share.toFixed(1)}%</span>
                        </div>
                    </td>
                </tr>
            `;
        });
        Elements.billMeterBreakdownBody.innerHTML = html;
    }
}

function renderReadingsTable(readings, filterQuery = '') {
    if (!Elements.fullReadingsTableBody) return;
    if (readings.length === 0) {
        Elements.fullReadingsTableBody.innerHTML = `<tr><td colspan="6" class="empty-state">No readings available.</td></tr>`;
        return;
    }

    let filtered = readings;
    if (filterQuery) {
        filtered = readings.filter(r =>
            r.readingId.toLowerCase().includes(filterQuery) ||
            r.meterId.toLowerCase().includes(filterQuery)
        );
    }

    if (filtered.length === 0) {
        Elements.fullReadingsTableBody.innerHTML = `<tr><td colspan="6" class="empty-state">No readings matched "${escapeHtml(filterQuery)}".</td></tr>`;
        return;
    }

    let html = '';
    filtered.forEach(r => {
        const meterInfo = getMeterInfo(r.meterId);
        const sourceBadge = r.isSimulated
            ? `<span class="badge badge-simulated">Simulated</span>`
            : `<span class="badge badge-live">Live</span>`;

        html += `
            <tr>
                <td><strong><code>${escapeHtml(r.readingId)}</code></strong></td>
                <td><code>${escapeHtml(r.meterId)}</code></td>
                <td>${escapeHtml(meterInfo.building)}</td>
                <td><strong>${r.energyConsumed.toFixed(2)} kWh</strong></td>
                <td>${formatTimestamp(r.timestamp)}</td>
                <td>${sourceBadge}</td>
            </tr>
        `;
    });
    Elements.fullReadingsTableBody.innerHTML = html;
}

// =============================================================================
// CHART RENDERING (Chart.js)
// =============================================================================

function renderTrendCharts(comp) {
    if (!window.Chart) return;

    const trend = comp.trend || [];
    const labels = trend.map(t => `${t.day} (${t.date.slice(5)})`);
    const values = trend.map(t => t.kwh);

    // Overview 7-day trend chart
    const ctxOverview = document.getElementById('chart-overview-trend');
    if (ctxOverview) {
        if (AppState.charts.overviewTrend) AppState.charts.overviewTrend.destroy();
        AppState.charts.overviewTrend = new Chart(ctxOverview, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Daily Energy (kWh)',
                    data: values,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.12)',
                    borderWidth: 2.5,
                    fill: true,
                    tension: 0.3,
                    pointBackgroundColor: '#10b981',
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { callback: v => v + ' kWh' }
                    }
                }
            }
        });
    }

    // Analytics detailed daily chart
    const ctxAnalyticsDaily = document.getElementById('chart-analytics-daily');
    if (ctxAnalyticsDaily) {
        if (AppState.charts.analyticsDaily) AppState.charts.analyticsDaily.destroy();
        AppState.charts.analyticsDaily = new Chart(ctxAnalyticsDaily, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Energy Consumed (kWh)',
                    data: values,
                    backgroundColor: '#10b981',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { callback: v => v + ' kWh' }
                    }
                }
            }
        });
    }

    // Analytics Meters Chart
    renderMetersChart();
}

function renderBuildingOverviewChart(buildings) {
    if (!window.Chart) return;
    const ctxBuildings = document.getElementById('chart-overview-buildings');
    if (!ctxBuildings) return;

    if (AppState.charts.overviewBuildings) AppState.charts.overviewBuildings.destroy();

    const labels = buildings.map(b => b.buildingName.replace(' Centre', '').replace(' Block', '').replace(' Complex', ''));
    const values = buildings.map(b => b.totalKwh);

    AppState.charts.overviewBuildings = new Chart(ctxBuildings, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: ['#10b981', '#0284c7', '#8b5cf6', '#f59e0b']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.label}: ${ctx.raw.toFixed(1)} kWh`
                    }
                }
            }
        }
    });
}

function renderMetersChart() {
    if (!window.Chart) return;
    const ctxMeters = document.getElementById('chart-analytics-meters');
    if (!ctxMeters) return;

    if (AppState.charts.analyticsMeters) AppState.charts.analyticsMeters.destroy();

    const labels = AppState.meters.map(m => m.meterId);
    const values = AppState.meters.map(m => m.totalKwh);

    AppState.charts.analyticsMeters = new Chart(ctxMeters, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Total Consumed (kWh)',
                data: values,
                backgroundColor: '#0284c7',
                borderRadius: 4
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: { callback: v => v + ' kWh' }
                }
            }
        }
    });
}

// =============================================================================
// USER ACTIONS & FORM HANDLERS
// =============================================================================

async function handleAddReadingSubmit(e) {
    e.preventDefault();

    const readingId = Elements.inputReadingId.value.trim();
    const meterId = Elements.selectMeterId.value.trim();
    const energyConsumed = parseFloat(Elements.inputEnergyVal.value);
    const timestamp = Elements.inputTimestamp.value;

    if (!readingId) {
        showToast('Validation Error', 'Reading ID is required.', 'error');
        return;
    }
    if (isNaN(energyConsumed) || energyConsumed < 0) {
        showToast('Validation Error', 'Energy value must be a non-negative finite number.', 'error');
        return;
    }

    try {
        const payload = {
            readingId: readingId,
            meterId: meterId,
            energyConsumed: energyConsumed,
            timestamp: timestamp || null
        };

        const res = await fetch('/api/readings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok && data.success) {
            showToast('Reading Added', `[WorkerThread] Reading ${readingId} recorded for meter ${meterId}.`, 'success');
            Elements.inputReadingId.value = '';
            Elements.inputEnergyVal.value = '';
            initTimestampField();
            // Reload all dashboard metrics
            await loadAllData();
        } else {
            showToast('Ingestion Error', data.error || 'Failed to add reading.', 'error');
        }
    } catch (err) {
        console.error('Add reading error:', err);
        showToast('Network Error', 'Could not send reading to Java backend.', 'error');
    }
}

async function handleRunConcurrentSimulation() {
    try {
        if (Elements.concurrentResultBox) {
            Elements.concurrentResultBox.style.display = 'block';
            Elements.concurrentResultBox.textContent = 'Spawning MeterThread-1 and MeterThread-2 for concurrent ingestion...';
        }

        const res = await fetch('/api/simulate-concurrent', { method: 'POST' });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast('Concurrency Test Passed', 'Simultaneous readings written safely by worker threads.', 'success');
            if (Elements.concurrentResultBox) {
                const ids = data.readings.map(r => `${r.readingId} (${r.meterId}: ${r.energyConsumed} kWh)`).join(' and ');
                Elements.concurrentResultBox.innerHTML = `
                    <strong>Concurrency Succeeded:</strong><br>
                    Recorded: ${escapeHtml(ids)}<br>
                    <em>Thread-safe synchronized collections in EnergyReadingManager.java verified!</em>
                `;
            }
            await loadAllData();
        } else {
            showToast('Simulation Failed', data.error || 'Concurrent ingestion error', 'error');
        }
    } catch (err) {
        console.error('Concurrency error:', err);
        showToast('Error', 'Failed to execute concurrent simulation.', 'error');
    }
}

async function handleLoadHistoricalSimulation() {
    try {
        if (Elements.historicalResultBox) {
            Elements.historicalResultBox.style.display = 'block';
            Elements.historicalResultBox.textContent = 'Injecting benchmark readings for yesterday and previous week...';
        }

        const res = await fetch('/api/simulate-historical', { method: 'POST' });
        const data = await res.json();

        if (res.ok && data.success) {
            showToast('Historical Data Loaded', `Injected ${data.addedCount} benchmark readings for past days & weeks.`, 'success');
            if (Elements.historicalResultBox) {
                Elements.historicalResultBox.innerHTML = `
                    <strong>Loaded Historical Test Readings:</strong><br>
                    Populated 4 benchmark records (SIM-HIST-YEST &amp; SIM-HIST-PREVWK).<br>
                    <em>Daily and weekly consumption comparisons now display rich percentage variance!</em>
                `;
            }
            await loadAllData();
        } else {
            showToast('Historical Loading Error', data.error || 'Failed to load historical data', 'error');
        }
    } catch (err) {
        console.error('Historical simulation error:', err);
        showToast('Error', 'Failed to load historical readings.', 'error');
    }
}

// =============================================================================
// AUTOMATIC LIVE MONITORING CONTROLS & STREAM SIMULATION
// =============================================================================

let autoMonitoringTimer = null;
let isAutoMonitoringActive = false;
let isAutoCycleInProgress = false;
let autoSimulatedCount = 0;

function handleStartMonitoring() {
    if (isAutoMonitoringActive) {
        // Prevent duplicate timers
        return;
    }

    isAutoMonitoringActive = true;

    // Update UI controls and badges
    if (Elements.btnStartMonitoring) Elements.btnStartMonitoring.disabled = true;
    if (Elements.btnStopMonitoring) Elements.btnStopMonitoring.disabled = false;

    if (Elements.autoMonitorBadge) {
        Elements.autoMonitorBadge.textContent = 'Active (5s)';
        Elements.autoMonitorBadge.style.backgroundColor = 'var(--primary-50)';
        Elements.autoMonitorBadge.style.color = 'var(--primary-700)';
        Elements.autoMonitorBadge.style.borderColor = 'var(--primary-100)';
    }

    if (Elements.autoMonitorPulse) {
        Elements.autoMonitorPulse.style.backgroundColor = 'var(--primary-500)';
        Elements.autoMonitorPulse.style.animation = 'pulse 1.8s infinite';
    }

    if (Elements.autoMonitorStatusText) {
        Elements.autoMonitorStatusText.innerHTML = '<strong>Live Stream Active:</strong> Simulating real-time meter readings every 5 seconds...';
        Elements.autoMonitorStatusText.style.color = 'var(--primary-700)';
    }

    if (Elements.headerMonitoringPill) {
        Elements.headerMonitoringPill.style.display = 'inline-flex';
    }

    showToast('Live Monitoring Started', 'Generating simulated meter readings every 5 seconds.', 'success');

    // Generate first reading immediately
    runAutoMonitoringCycle();

    // Schedule subsequent readings every 5 seconds (5000 ms)
    autoMonitoringTimer = setInterval(() => {
        runAutoMonitoringCycle();
    }, 5000);
}

function handleStopMonitoring() {
    if (autoMonitoringTimer) {
        clearInterval(autoMonitoringTimer);
        autoMonitoringTimer = null;
    }

    isAutoMonitoringActive = false;

    // Update UI controls and badges
    if (Elements.btnStartMonitoring) Elements.btnStartMonitoring.disabled = false;
    if (Elements.btnStopMonitoring) Elements.btnStopMonitoring.disabled = true;

    if (Elements.autoMonitorBadge) {
        Elements.autoMonitorBadge.textContent = 'Stopped';
        Elements.autoMonitorBadge.style.backgroundColor = 'var(--danger-soft)';
        Elements.autoMonitorBadge.style.color = 'var(--danger)';
        Elements.autoMonitorBadge.style.borderColor = '#fecaca';
    }

    if (Elements.autoMonitorPulse) {
        Elements.autoMonitorPulse.style.backgroundColor = 'var(--text-muted)';
        Elements.autoMonitorPulse.style.animation = 'none';
    }

    if (Elements.autoMonitorStatusText) {
        Elements.autoMonitorStatusText.innerHTML = `<strong>Monitoring Paused:</strong> Inactive. Total simulated readings generated: ${autoSimulatedCount}.`;
        Elements.autoMonitorStatusText.style.color = 'var(--text-secondary)';
    }

    if (Elements.headerMonitoringPill) {
        Elements.headerMonitoringPill.style.display = 'none';
    }

    showToast('Monitoring Stopped', 'Automatic reading generation has been paused.', 'info');
}

async function runAutoMonitoringCycle() {
    if (!isAutoMonitoringActive) return;

    // Prevent overlapping requests
    if (isAutoCycleInProgress) {
        console.warn('Auto monitoring cycle skipped: previous request still in progress.');
        return;
    }

    isAutoCycleInProgress = true;

    try {
        // Meter candidates: MTR-101, MTR-102, or MTR-ENG-01
        const meters = ['MTR-101', 'MTR-102', 'MTR-ENG-01'];
        const selectedMeterId = meters[Math.floor(Math.random() * meters.length)];

        // Generate unique reading ID with SIM-AUTO- prefix
        const uniqueSuffix = Date.now().toString().slice(-6) + '-' + Math.floor(Math.random() * 900 + 100);
        const readingId = `SIM-AUTO-${uniqueSuffix}`;

        // Generate realistic positive energy value:
        // ~25% chance of high usage reading (105.0 - 148.0 kWh) to demonstrate anomaly detection (> 100 kWh threshold)
        // ~75% chance of normal usage (22.0 - 68.0 kWh)
        const isAnomalySpike = Math.random() < 0.25;
        let energyVal;
        if (isAnomalySpike) {
            energyVal = +(105.0 + Math.random() * 43.0).toFixed(2);
        } else {
            energyVal = +(22.0 + Math.random() * 46.0).toFixed(2);
        }

        const now = new Date();
        const timestamp = now.toISOString();

        const payload = {
            readingId: readingId,
            meterId: selectedMeterId,
            energyConsumed: energyVal,
            timestamp: timestamp
        };

        const res = await fetch('/api/simulate-auto', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (res.ok && data.success && data.reading) {
            autoSimulatedCount++;

            if (Elements.autoMonitorCount) {
                Elements.autoMonitorCount.textContent = `Simulated readings: ${autoSimulatedCount}`;
            }

            const r = data.reading;
            const meterInfo = getMeterInfo(r.meterId);
            const isAnomaly = r.energyConsumed > AppState.anomalyThreshold;

            if (Elements.autoMonitorDetails) {
                Elements.autoMonitorDetails.style.display = 'block';
                Elements.autoMonitorDetails.innerHTML = `
                    Latest Ingested: <strong><code>${escapeHtml(r.readingId)}</code></strong> &bull;
                    Meter: <strong><code>${escapeHtml(r.meterId)}</code></strong> (${escapeHtml(meterInfo.building)} &bull; ${escapeHtml(meterInfo.appliance)}) &bull;
                    Energy: <strong style="color: ${isAnomaly ? 'var(--danger)' : 'var(--primary-700)'};">${r.energyConsumed.toFixed(2)} kWh</strong>
                    ${isAnomaly ? '<span class="badge badge-anomaly-high" style="margin-left: 6px;">⚠️ Spike (&gt; ' + AppState.anomalyThreshold + ' kWh)</span>' : '<span class="badge badge-live" style="margin-left: 6px;">Normal</span>'}
                    &bull; <small style="color: var(--text-muted);">${new Date().toLocaleTimeString()}</small>
                `;
            }

            // If an anomaly was detected, raise a distinct warning toast
            if (isAnomaly) {
                showToast('Anomaly Detected!', `[Simulated Stream] Meter ${r.meterId} registered spike of ${r.energyConsumed.toFixed(2)} kWh!`, 'warning');
            }

            // Refresh all dashboard metrics, charts, readings table, bill estimation, and anomaly alerts
            await loadAllData();
        } else {
            showToast('Simulation Error', data.error || 'Failed to record simulated reading.', 'error');
        }
    } catch (err) {
        console.error('Auto monitoring cycle error:', err);
        showToast('Stream Error', 'Failed to communicate with Java server for live monitoring.', 'error');
    } finally {
        isAutoCycleInProgress = false;
    }
}

// =============================================================================
// UTILITIES & HELPERS
// =============================================================================

function getMeterInfo(meterId) {
    if (!AppState.meters || AppState.meters.length === 0) {
        return { building: 'Campus Grid', room: 'Main Facility', appliance: 'General Load' };
    }
    const match = AppState.meters.find(m => m.meterId === meterId);
    if (match) {
        return {
            building: match.building,
            room: match.room,
            appliance: match.appliance
        };
    }
    return { building: 'Campus Facility', room: 'Distribution', appliance: 'Smart Load' };
}

function formatTimestamp(tsStr) {
    if (!tsStr) return 'N/A';
    try {
        const d = new Date(tsStr);
        if (isNaN(d.getTime())) return tsStr;
        return d.toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return tsStr;
    }
}

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showToast(title, message, type = 'info') {
    if (!Elements.toastContainer) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = {
        'success': '✅',
        'error': '❌',
        'info': 'ℹ️',
        'warning': '⚠️'
    };

    toast.innerHTML = `
        <div class="toast-icon">${icons[type] || 'ℹ️'}</div>
        <div class="toast-text">
            <h5>${escapeHtml(title)}</h5>
            <p>${escapeHtml(message)}</p>
        </div>
    `;

    Elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}
