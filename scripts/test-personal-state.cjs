const {test} = require('node:test');
const assert = require('node:assert/strict');
const {readFileSync} = require('node:fs');
const {join} = require('node:path');
const vm = require('node:vm');
const source = readFileSync(join(__dirname, '../product/demo/personal-state.js'), 'utf8');
function client() {
  const data = new Map();
  const localStorage = {getItem:k=>data.get(k)||null, setItem:(k,v)=>data.set(k,v)};
  const context = vm.createContext({localStorage, fetch:()=>{throw Error('Personal data must stay local')}});
  vm.runInContext(source, context);return {api:context.NextStopPersonal,context};
}
const room = {code:'KB-227', building:'Knowles Building', factors:{quiet:1,desk:1,sockets:1,aircon:1,space:1,light:1}};
test('personal history changes only its local profile and survives module reload',()=>{
 const {api,context}=client();
 for(let i=0;i<10;i++)api.recordVisit(room.building,'a');
 assert.equal(api.score(room,'a',0).staticScore,1);
 assert.ok(Math.abs(api.score(room,'b',0).staticScore-.8)<1e-12);
 vm.runInContext(source,context);
 assert.equal(context.NextStopPersonal.score(room,'a',0).familiarity,1);
});
test('rejection is local, decays over 21 days and undo restores score',()=>{
 const {api}=client(),now=100000;
 const id=api.recordRejection('KB 227',.5,'a',now);
 assert.equal(api.score(room,'a',now).nEff,.5);
 assert.ok(Math.abs(api.score(room,'a',now+21*86400000).nEff-.5/Math.E)<1e-12);
 assert.equal(api.score(room,'b',now).nEff,0);
 api.undoRejection(id,'a');assert.equal(api.score(room,'a',now).nEff,0);
});
test('missing factors renormalise, an unmeasured room has no score',()=>{
 const {api}=client();for(let i=0;i<10;i++)api.recordVisit(room.building,'a');
 assert.ok(Math.abs(api.score({...room,factors:{quiet:.5}},'a').staticScore-.75)<1e-12);
 assert.equal(api.score({...room,factors:{}},'a').score,null);
});
test('15-minute threshold accepts exactly 15 and 16, rejects 14',()=>{
 const {api}=client();assert.equal(api.isStudyDuration(14),false);
 assert.equal(api.isStudyDuration(15),true);assert.equal(api.isStudyDuration(16),true);
});
