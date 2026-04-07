/**
 * QUESTION PAPER GENERATOR — app.js
 * Mirrors the logic from Main.java (terminal → browser)
 *
 * Flow:
 *  Step 1 → Subject name + chapter count
 *  Step 2 → Per-chapter name + questions (text + mark type)
 *  Step 3 → Total marks + chapter weightage %
 *  Step 4 → Generated paper (random selection, mark-budget algorithm)
 */

// ================================================================
//  STATE
// ================================================================
const state = {
  currentStep: 1,
  subjectName: '',
  chapterCount: 0,
  chapters: [],      // [{ name, questions: [{text, marks}] }]
  totalMarks: 0,
  weightage: {},     // { chapterName: percent }
  generatedPaper: [] // [{ chapter, text, marks }]
};

// ================================================================
//  DOM HELPERS
// ================================================================
function $(id) { return document.getElementById(id); }

function showStep(n) {
  document.querySelectorAll('.step-section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.step-item').forEach(item => {
    const s = parseInt(item.dataset.step);
    item.classList.remove('active','done');
    if (s === n) item.classList.add('active');
    else if (s < n) item.classList.add('done');
  });
  $(`step-${n}`).classList.add('active');
  state.currentStep = n;
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function toast(msg, type = '') {
  let el = document.querySelector('.toast');
  if (!el) {
    el = document.createElement('div');
    el.className = 'toast';
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.className = 'toast ' + type;
  // Force reflow so transition triggers
  void el.offsetWidth;
  el.classList.add('show');
  setTimeout(() => el.classList.remove('show'), 3000);
}

function validate(value, label) {
  if (!value || (typeof value === 'string' && !value.trim())) {
    toast(`"${label}" cannot be empty.`, 'error');
    return false;
  }
  return true;
}

// ================================================================
//  STEP 1 → STEP 2
// ================================================================
$('step1Next').addEventListener('click', () => {
  const name = $('subjectName').value.trim();
  const count = parseInt($('chapterCount').value);

  if (!validate(name, 'Subject Name')) return;
  if (!count || count < 1) { toast('Enter at least 1 chapter.', 'error'); return; }
  if (count > 20)          { toast('Maximum 20 chapters allowed.', 'error'); return; }

  state.subjectName = name;
  state.chapterCount = count;

  buildChapterForms(count);
  showStep(2);
});

// ================================================================
//  BUILD CHAPTER FORMS (Step 2)
// ================================================================
function buildChapterForms(count) {
  const container = $('chaptersContainer');
  container.innerHTML = '';

  for (let i = 0; i < count; i++) {
    const card = document.createElement('div');
    card.className = 'chapter-card';
    card.id = `chapter-card-${i}`;
    card.innerHTML = `
      <div class="chapter-card-header">
        <span class="chapter-number">Chapter ${i + 1}</span>
        <input type="text" class="input-field chapter-name-input"
               data-idx="${i}" placeholder="Chapter name"
               style="flex:1; margin-left:12px; margin-bottom:0;"
               value="${state.chapters[i]?.name || ''}" />
      </div>
      <div class="chapter-card-body">
        <div class="questions-list" id="questions-list-${i}"></div>
        <button class="btn-add-question" data-chapter="${i}">+ Add Question</button>
      </div>
    `;
    container.appendChild(card);

    // Restore previous questions if any
    const existing = state.chapters[i]?.questions || [];
    if (existing.length > 0) {
      existing.forEach(q => addQuestionRow(i, q.text, q.marks));
    } else {
      addQuestionRow(i); // default empty row
    }
  }

  // Delegate add-question
  container.addEventListener('click', e => {
    if (e.target.classList.contains('btn-add-question')) {
      const idx = parseInt(e.target.dataset.chapter);
      addQuestionRow(idx);
    }
    if (e.target.classList.contains('btn-remove')) {
      const row = e.target.closest('.question-row');
      if (row) row.remove();
    }
  });
}

function addQuestionRow(chapterIdx, text = '', marks = 2) {
  const list = $(`questions-list-${chapterIdx}`);
  const row = document.createElement('div');
  row.className = 'question-row';
  row.innerHTML = `
    <input type="text" class="input-field q-text-input" placeholder="Question text" value="${escapeHtml(text)}" />
    <select class="mark-select q-mark-select">
      <option value="2"  ${marks == 2 ? 'selected' : ''}>2 Marks</option>
      <option value="8"  ${marks == 8 ? 'selected' : ''}>8 Marks</option>
    </select>
    <button class="btn-remove" title="Remove">×</button>
  `;
  list.appendChild(row);
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g,'&amp;')
    .replace(/"/g,'&quot;')
    .replace(/</g,'&lt;')
    .replace(/>/g,'&gt;');
}

// ================================================================
//  STEP 2 → STEP 3  /  BACK
// ================================================================
$('step2Next').addEventListener('click', () => {
  const chapters = collectChapters();
  if (!chapters) return;
  state.chapters = chapters;
  buildWeightageForm(chapters);
  showStep(3);
});

$('step2Back').addEventListener('click', () => showStep(1));

function collectChapters() {
  const cards = document.querySelectorAll('.chapter-card');
  const chapters = [];

  for (let i = 0; i < cards.length; i++) {
    const nameInput = cards[i].querySelector('.chapter-name-input');
    const chName = nameInput.value.trim();
    if (!chName) { toast(`Chapter ${i+1} needs a name.`, 'error'); return null; }

    const qRows = cards[i].querySelectorAll('.question-row');
    const questions = [];
    let valid = true;

    qRows.forEach((row, j) => {
      const text  = row.querySelector('.q-text-input').value.trim();
      const marks = parseInt(row.querySelector('.q-mark-select').value);
      if (!text) {
        toast(`Chapter ${i+1}, Question ${j+1} text is empty.`, 'error');
        valid = false;
        return;
      }
      questions.push({ text, marks });
    });

    if (!valid) return null;
    if (questions.length === 0) {
      toast(`Chapter "${chName}" has no questions.`, 'error');
      return null;
    }

    chapters.push({ name: chName, questions });
  }
  return chapters;
}

// ================================================================
//  BUILD WEIGHTAGE FORM (Step 3)
// ================================================================
function buildWeightageForm(chapters) {
  const container = $('weightageContainer');
  container.innerHTML = '';

  chapters.forEach((ch, i) => {
    const card = document.createElement('div');
    card.className = 'weightage-card';
    card.innerHTML = `
      <div>
        <div class="field-label">${ch.name}</div>
        <div style="font-size:12px;color:var(--text-light);">${ch.questions.length} question${ch.questions.length !== 1 ? 's' : ''}</div>
      </div>
      <div style="display:flex;align-items:center;gap:6px;">
        <input type="number" class="input-field input-narrow w-input"
               data-chapter="${ch.name}" min="0" max="100" placeholder="0"
               value="${state.weightage[ch.name] || ''}" />
        <span style="font-family:'Syne',sans-serif;font-size:16px;font-weight:700;color:var(--text-mid);">%</span>
      </div>
    `;
    container.appendChild(card);
  });

  // Live total checker
  container.addEventListener('input', () => updateWeightageStatus());
}

function updateWeightageStatus() {
  const inputs = document.querySelectorAll('.w-input');
  let total = 0;
  inputs.forEach(inp => { total += parseInt(inp.value) || 0; });

  const el = $('weightageStatus');
  el.classList.add('show');

  if (total === 100) {
    el.className = 'weightage-status show ok';
    el.textContent = `✓ Total weightage: 100% — perfect!`;
  } else if (total > 100) {
    el.className = 'weightage-status show err';
    el.textContent = `⚠ Total weightage: ${total}% — exceeds 100%.`;
  } else {
    el.className = 'weightage-status show warn';
    el.textContent = `Total weightage: ${total}% — paper marks may not match target.`;
  }
}

// ================================================================
//  GENERATE PAPER  (Step 3 → Step 4)
// ================================================================
$('step3Back').addEventListener('click', () => showStep(2));

$('generateBtn').addEventListener('click', () => {
  const totalMarks = parseInt($('totalMarks').value);
  if (!totalMarks || totalMarks < 1) { toast('Enter total marks.', 'error'); return; }

  const inputs = document.querySelectorAll('.w-input');
  const weightage = {};
  let wTotal = 0;
  let valid = true;

  inputs.forEach(inp => {
    const val = parseInt(inp.value) || 0;
    if (val < 0) { toast('Weightage cannot be negative.', 'error'); valid = false; }
    weightage[inp.dataset.chapter] = val;
    wTotal += val;
  });

  if (!valid) return;
  if (wTotal === 0) { toast('Enter at least some weightage.', 'error'); return; }

  state.totalMarks = totalMarks;
  state.weightage  = weightage;

  if (wTotal !== 100) {
    // Warn but allow
    if (!confirm(`Weightages add up to ${wTotal}%, not 100%. Continue anyway?`)) return;
  }

  const paper = generatePaper(state.chapters, totalMarks, weightage);
  state.generatedPaper = paper;

  renderPaper(paper, totalMarks, wTotal);
  showStep(4);
});

// ================================================================
//  CORE ALGORITHM — mirrors QuestionPaperGenerator.generate()
//  Mark-budget per chapter, 8-mark first, then 2-mark fill
// ================================================================
function generatePaper(chapters, totalMarks, weightage) {
  const paper = [];

  chapters.forEach(chapter => {
    const percent = weightage[chapter.name] || 0;
    const budget  = Math.floor((totalMarks * percent) / 100);

    // Separate pools
    const eightPool = chapter.questions.filter(q => q.marks === 8);
    const twoPool   = chapter.questions.filter(q => q.marks === 2);

    // Shuffle (Fisher-Yates)
    shuffle(eightPool);
    shuffle(twoPool);

    let remaining = budget;

    eightPool.forEach(q => {
      if (remaining >= 8) {
        paper.push({ chapter: chapter.name, text: q.text, marks: 8 });
        remaining -= 8;
      }
    });

    twoPool.forEach(q => {
      if (remaining >= 2) {
        paper.push({ chapter: chapter.name, text: q.text, marks: 2 });
        remaining -= 2;
      }
    });

    if (remaining > 0 && budget > 0) {
      console.warn(`[WARN] ${chapter.name}: short by ${remaining} marks (budget ${budget})`);
    }
  });

  return paper;
}

function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

// ================================================================
//  RENDER GENERATED PAPER
// ================================================================
function renderPaper(paper, targetMarks, wTotal) {
  const out = $('paperOutput');
  out.innerHTML = '';

  const actualMarks = paper.reduce((s, q) => s + q.marks, 0);

  // Meta header
  const meta = document.createElement('div');
  meta.className = 'paper-meta';
  meta.innerHTML = `
    <div class="paper-meta-item">
      <label>Subject</label>
      <span>${escapeHtml(state.subjectName)}</span>
    </div>
    <div class="paper-meta-item">
      <label>Total Questions</label>
      <span>${paper.length}</span>
    </div>
    <div class="paper-meta-item">
      <label>Target Marks</label>
      <span>${targetMarks}</span>
    </div>
    <div class="paper-meta-item">
      <label>Actual Marks</label>
      <span class="big">${actualMarks}</span>
    </div>
  `;
  out.appendChild(meta);

  // Warnings
  if (wTotal !== 100) {
    const w = document.createElement('div');
    w.className = 'paper-warning';
    w.textContent = `⚠ Weightages summed to ${wTotal}%, so actual marks (${actualMarks}) may differ from target (${targetMarks}).`;
    out.appendChild(w);
  }

  if (paper.length === 0) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.textContent = 'No questions could be selected. Try increasing marks or adjusting weightage.';
    out.appendChild(empty);
    return;
  }

  // Group by chapter
  const byChapter = {};
  paper.forEach(q => {
    if (!byChapter[q.chapter]) byChapter[q.chapter] = [];
    byChapter[q.chapter].push(q);
  });

  let qNum = 1;
  Object.entries(byChapter).forEach(([chName, qs]) => {
    const block = document.createElement('div');
    block.className = 'paper-chapter-block';
    block.innerHTML = `<div class="paper-chapter-title">${escapeHtml(chName)}</div>`;

    qs.forEach(q => {
      const item = document.createElement('div');
      item.className = 'question-item';
      item.innerHTML = `
        <span class="q-num">Q${qNum}.</span>
        <span class="q-text">${escapeHtml(q.text)}</span>
        <span class="mark-pill ${q.marks === 2 ? 'two' : 'eight'}">${q.marks} Marks</span>
      `;
      block.appendChild(item);
      qNum++;
    });

    out.appendChild(block);
  });
}

// ================================================================
//  STEP 4 NAVIGATION
// ================================================================
$('step4Back').addEventListener('click', () => showStep(3));

// ================================================================
//  DOWNLOAD AS .TXT  — mirrors FileHandler.writeToFile()
// ================================================================
$('downloadBtn').addEventListener('click', () => {
  const paper = state.generatedPaper;
  if (paper.length === 0) { toast('No paper to download.', 'error'); return; }

  const actualMarks = paper.reduce((s, q) => s + q.marks, 0);
  const lines = [];

  lines.push('============================================');
  lines.push('           QUESTION PAPER');
  lines.push(`Subject      : ${state.subjectName}`);
  lines.push(`Total Marks  : ${actualMarks}`);
  lines.push(`Questions    : ${paper.length}`);
  lines.push('============================================');
  lines.push('');

  // Group by chapter
  const byChapter = {};
  paper.forEach(q => {
    if (!byChapter[q.chapter]) byChapter[q.chapter] = [];
    byChapter[q.chapter].push(q);
  });

  let num = 1;
  Object.entries(byChapter).forEach(([ch, qs]) => {
    lines.push(`--- ${ch} ---`);
    qs.forEach(q => {
      lines.push(`  Q${num}. [${q.marks} Marks] ${q.text}`);
      num++;
    });
    lines.push('');
  });

  lines.push('============================================');
  lines.push('             END OF PAPER');
  lines.push('============================================');

  const blob = new Blob([lines.join('\n')], { type: 'text/plain' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `${state.subjectName.replace(/\s+/g,'_')}_QuestionPaper.txt`;
  a.click();
  URL.revokeObjectURL(a.href);
  toast('Paper downloaded!', 'success');
});

// ================================================================
//  INIT
// ================================================================
showStep(1);
