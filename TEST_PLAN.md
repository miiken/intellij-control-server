# Test Plan: Rename Refactoring

This document provides comprehensive testing instructions for the IntelliJ Control Server plugin's rename refactoring capabilities.

## Test Setup

1. **Open Project in IntelliJ**
   - Ensure the IntelliJ Control Server plugin is installed and running
   - Verify the server is accessible: `curl http://localhost:8767/health`

2. **Test Files Location**
   - Test files: `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/{language}/SampleClass.{ext}`
   - Expected results: `src/test/resources/expected/{language}/SampleClass_after*.{ext}`

3. **API Base URL**
   - HTTP API: `http://localhost:8767`
   - Project name: `intellij-control-server` (or your actual project name)

---

## Test Suite 1: Kotlin Rename Tests

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt`

### Test 1.1: Rename Class (Kotlin)

**Operation:** Rename class `SampleCalculator` to `Calculator`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 12,
    "oldName": "SampleCalculator",
    "newName": "Calculator"
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
- Class declaration on line 12 changes from `class SampleCalculator` to `class Calculator`

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
```

---

### Test 1.2: Rename Field (Kotlin)

**Operation:** Rename field `oldFieldName` to `counter`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 15,
    "oldName": "oldFieldName",
    "newName": "counter"
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
- Line 15: `private var oldFieldName` → `private var counter`
- Line 21: `return oldVariableName + oldFieldName` → `return oldVariableName + counter`
- Line 33: `oldFieldName += bonusPoints` → `counter += bonusPoints`
- Code should still compile (all references updated)

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
```

---

### Test 1.3: Rename Method (Kotlin)

**Operation:** Rename method `oldMethodName` to `calculate`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 18,
    "oldName": "oldMethodName",
    "newName": "calculate"
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
- Line 18: `fun oldMethodName` → `fun calculate`
- Line 32: `val bonusPoints = oldMethodName(total.toInt())` → `val bonusPoints = calculate(total.toInt())`
- Line 45: `oldMethodName(amount.toInt())` → `calculate(amount.toInt())`
- Code should still compile (all call sites updated)

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
```

---

### Test 1.4: Rename Parameter (Kotlin)

**Operation:** Rename parameter `oldParameterName` to `value`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 18,
    "oldName": "oldParameterName",
    "newName": "value"
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
- Line 18: `oldParameterName: Int` → `value: Int`
- Line 20: `oldParameterName * 2` → `value * 2`

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
```

---

### Test 1.5: Rename Local Variable (Kotlin)

**Operation:** Rename local variable `oldVariableName` to `result`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 20,
    "oldName": "oldVariableName",
    "newName": "result"
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
- Line 20: `val oldVariableName` → `val result`
- Line 21: `return oldVariableName` → `return result`

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
```

---

## Test Suite 2: JavaScript Rename Tests

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js`

### Test 2.1: Rename Class (JavaScript)

**Operation:** Rename class `SampleCalculator` to `Calculator`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 11,
    "oldName": "SampleCalculator",
    "newName": "Calculator"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
```

---

### Test 2.2: Rename Field (JavaScript)

**Operation:** Rename field `oldFieldName` to `counter`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 14,
    "oldName": "oldFieldName",
    "newName": "counter"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
```

---

### Test 2.3: Rename Method (JavaScript)

**Operation:** Rename method `oldMethodName` to `calculate`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 18,
    "oldName": "oldMethodName",
    "newName": "calculate"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
```

---

### Test 2.4: Rename Parameter (JavaScript)

**Operation:** Rename parameter `oldParameterName` to `value`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 18,
    "oldName": "oldParameterName",
    "newName": "value"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
```

---

### Test 2.5: Rename Local Variable (JavaScript)

**Operation:** Rename local variable `oldVariableName` to `result`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 20,
    "oldName": "oldVariableName",
    "newName": "result"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
```

---

## Test Suite 3: TypeScript Rename Tests

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts`

### Test 3.1: Rename Class (TypeScript)

**Operation:** Rename class `SampleCalculator` to `Calculator`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 11,
    "oldName": "SampleCalculator",
    "newName": "Calculator"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
```

---

### Test 3.2: Rename Field (TypeScript)

**Operation:** Rename field `oldFieldName` to `counter`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 13,
    "oldName": "oldFieldName",
    "newName": "counter"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
```

---

### Test 3.3: Rename Method (TypeScript)

**Operation:** Rename method `oldMethodName` to `calculate`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 16,
    "oldName": "oldMethodName",
    "newName": "calculate"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
```

---

### Test 3.4: Rename Parameter (TypeScript)

**Operation:** Rename parameter `oldParameterName` to `value`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 16,
    "oldName": "oldParameterName",
    "newName": "value"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
```

---

### Test 3.5: Rename Local Variable (TypeScript)

**Operation:** Rename local variable `oldVariableName` to `result`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 18,
    "oldName": "oldVariableName",
    "newName": "result"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
```

---

## Test Suite 4: Scala Rename Tests

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala`

### Test 4.1: Rename Class (Scala)

**Operation:** Rename class `SampleCalculator` to `Calculator`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 13,
    "oldName": "SampleCalculator",
    "newName": "Calculator"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala
```

---

### Test 4.2: Rename Field (Scala)

**Operation:** Rename field `oldFieldName` to `counter`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 15,
    "oldName": "oldFieldName",
    "newName": "counter"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala
```

---

### Test 4.3: Rename Method (Scala)

**Operation:** Rename method `oldMethodName` to `calculate`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 18,
    "oldName": "oldMethodName",
    "newName": "calculate"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala
```

---

### Test 4.4: Rename Parameter (Scala)

**Operation:** Rename parameter `oldParameterName` to `value`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 18,
    "oldName": "oldParameterName",
    "newName": "value"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala
```

---

### Test 4.5: Rename Local Variable (Scala)

**Operation:** Rename local variable `oldVariableName` to `result`

**API Call:**
```bash
curl -X POST http://localhost:8767/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 20,
    "oldName": "oldVariableName",
    "newName": "result"
  }'
```

**Revert:**
```bash
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala
```

---

## Summary

This test plan covers **20 test cases** across 4 languages:
- **5 symbol types**: Class, Field, Method, Parameter, Local Variable
- **4 languages**: Kotlin, JavaScript, TypeScript, Scala

Each test verifies that IntelliJ's rename refactoring engine correctly:
1. Renames the symbol at the specified location
2. Updates all references to that symbol
3. Returns accurate metadata (files changed, change count)
