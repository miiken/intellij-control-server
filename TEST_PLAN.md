# Test Plan: Refactoring Operations

This document provides comprehensive manual testing instructions for the IntelliJ Control Server plugin's refactoring capabilities.

## Test Setup

1. **Open Project in IntelliJ**
   - Ensure the IntelliJ Control Server plugin is installed and running
   - Verify the server is accessible: `curl http://localhost:8767/health`

2. **Test Files Location**
   - Original test files: `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/{language}/SampleClass.{ext}`
   - Expected results: `src/test/resources/expected/{language}/SampleClass_afterRename.{ext}`

3. **API Base URL**
   - HTTP API: `http://localhost:8767`
   - Project name: `intellij-control-server` (or your actual project name)

---

## Test Suite 1: Kotlin Rename

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt`

### Test 1.1: Rename Method (Kotlin)

**Operation:** Rename `newMethodName` to `renamedMethod`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 13,
    "oldName": "newMethodName",
    "newName": "renamedMethod"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "filesChanged": ["src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt"],
  "changesCount": 1
}
```

**Verification:**
- Compare the modified file with `src/test/resources/expected/kotlin/SampleClass_afterRename.kt`
- Ensure method name changed from `newMethodName` to `renamedMethod`
- Verify IntelliJ's refactoring engine was used (all references updated)

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
```

---

## Test Suite 2: JavaScript Rename

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js`

### Test 2.1: Rename Method (JavaScript)

**Operation:** Rename `newMethodName` to `renamedMethod`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 7,
    "oldName": "newMethodName",
    "newName": "renamedMethod"
  }'
```

**Verification:**
- Compare with `src/test/resources/expected/javascript/SampleClass_afterRename.js`

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
```

---

## Test Suite 3: TypeScript Rename

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts`

### Test 3.1: Rename Method (TypeScript)

**Operation:** Rename `newMethodName` to `renamedMethod`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 7,
    "oldName": "newMethodName",
    "newName": "renamedMethod"
  }'
```

**Verification:**
- Compare with `src/test/resources/expected/typescript/SampleClass_afterRename.ts`

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
```

---

## Test Suite 4: Scala Rename

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala`

### Test 4.1: Rename Method (Scala)

**Operation:** Rename `newMethodName` to `renamedMethod`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 7,
    "oldName": "newMethodName",
    "newName": "renamedMethod"
  }'
```

**Verification:**
- Compare with `src/test/resources/expected/scala/SampleClass_afterRename.scala`

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala
```

---

## Automated Test Runner

To run all tests sequentially:

```bash
#!/bin/bash
# Test all rename operations
echo "Testing Kotlin rename..."
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{"filePath":"src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt","line":13,"oldName":"newMethodName","newName":"renamedMethod"}'

echo -e "\n\nTesting JavaScript rename..."
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{"filePath":"src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js","line":7,"oldName":"newMethodName","newName":"renamedMethod"}'

echo -e "\n\nTesting TypeScript rename..."
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{"filePath":"src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts","line":7,"oldName":"newMethodName","newName":"renamedMethod"}'

echo -e "\n\nTesting Scala rename..."
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{"filePath":"src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala","line":7,"oldName":"newMethodName","newName":"renamedMethod"}'

echo -e "\n\n✅ All tests complete. Review results and revert changes with: git checkout src/test/"
```

---

## Notes

- All rename operations use IntelliJ's native refactoring engine
- The plugin properly handles threading (EDT, read actions, write actions)
- Test files are located in `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/`
- Always revert test files after testing to maintain clean state
