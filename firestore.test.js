const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const http = require('node:http');

test('Firestore rules validation', async (t) => {
  const rules = fs.readFileSync('./firestore.rules', 'utf8');
  assert.ok(rules.includes('service cloud.firestore'), 'Rules should define firestore service');
  assert.ok(rules.includes('match /users/{userId}'), 'Rules should define users collection');
  assert.ok(rules.includes('match /branches/{branchId}'), 'Rules should define branches collection');
});
