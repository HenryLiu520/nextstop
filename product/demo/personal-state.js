/* Personal recommendation data stays in this browser, scoped to a local profile. */
(function (root) {
  const WEIGHTS = Object.freeze({familiar: .20, quiet: .20, desk: .15, sockets: .15, aircon: .10, space: .10, light: .10});
  const MIN_STUDY_MINUTES = 15;
  const codeKey = value => String(value).toLowerCase().replace(/[^a-z0-9]/g, '');
  const isStudyDuration = minutes => Number.isFinite(minutes) && minutes >= MIN_STUDY_MINUTES;
  const key = profile => 'ns.personal.v1:' + encodeURIComponent(profile || 'device');
  function load(profile) {
    try { return JSON.parse(root.localStorage.getItem(key(profile))) || {visits: {}, rejections: []}; }
    catch { return {visits: {}, rejections: []}; }
  }
  function save(profile, state) { root.localStorage.setItem(key(profile), JSON.stringify(state)); }
  function recordVisit(building, profile) {
    const state = load(profile);
    state.visits[building] = (state.visits[building] || 0) + 1;
    save(profile, state);
  }
  function recordRejection(code, weight = 1, profile, now = Date.now()) {
    if (!(weight > 0 && weight <= 1)) throw new Error('Invalid rejection weight');
    const state = load(profile), id = String(now) + '-' + Math.random().toString(36).slice(2);
    state.rejections.push({id, code, weight, at: now}); save(profile, state); return id;
  }
  function undoRejection(id, profile) {
    const state = load(profile); state.rejections = state.rejections.filter(r => r.id !== id); save(profile, state);
  }
  function score(room, profile, now = Date.now()) {
    const state = load(profile), factors = {...room.factors};
    factors.familiar = Math.min((state.visits[room.building] || 0) / 10, 1);
    let numerator = 0, denominator = 0;
    for (const [factor, weight] of Object.entries(WEIGHTS)) {
      const value = factors[factor];
      if (typeof value !== 'number' || !Number.isFinite(value)) continue;
      numerator += weight * Math.max(0, Math.min(value, 1)); denominator += weight;
    }
    // No public quality measurements means no quality score yet.
    const measured = Object.keys(room.factors || {}).some(k => k !== 'familiar' && WEIGHTS[k] && Number.isFinite(room.factors[k]));
    const S = measured && denominator ? numerator / denominator : null;
    const nEff = state.rejections.filter(r => codeKey(r.code) === codeKey(room.code)).reduce((sum, r) =>
      sum + r.weight * Math.exp(-Math.max(0, now - r.at) / (21 * 86400000)), 0);
    const sPrime = S === null ? null : S * Math.pow(.88, nEff);
    return {...room, score: sPrime === null ? null : Math.round(sPrime * 100) / 10, staticScore: S,
      sPrime, nEff, familiarity: factors.familiar};
  }
  root.NextStopPersonal = Object.freeze({WEIGHTS, MIN_STUDY_MINUTES, isStudyDuration, load, recordVisit, recordRejection, undoRejection, score});
})(globalThis);
