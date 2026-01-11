# Test Plan: Native Extract Method Refactoring

This document provides comprehensive manual testing instructions for the IntelliJ Control Server plugin's refactoring capabilities, specifically testing the new native extract method implementation.

## Test Setup

1. **Open Project in IntelliJ**
   - Ensure the IntelliJ Control Server plugin is installed and running
   - Verify the server is accessible: `curl http://localhost:8765/health`

2. **Test Files Location**
   - Original test files: `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/{language}/SampleClass.{ext}`
   - Expected results: `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/{language}/SampleClass_after*.{ext}`

3. **API Base URL**
   - HTTP API: `http://localhost:8765`
   - Project name: `intellij-control-server` (or your actual project name)

---

## Test Suite 1: Kotlin Refactoring

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt`

### Test 1.1: Rename Method (Kotlin)

**Operation:** Rename `oldMethodName` to `newMethodName`

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "line": 12,
    "oldName": "oldMethodName",
    "newName": "newMethodName"
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

**Expected Code Change:**
```kotlin
// Line 12: Before
fun oldMethodName(value: Int): Int {

// Line 12: After
fun newMethodName(value: Int): Int {
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/kotlin/SampleClass_afterRename.kt`

**Verification:**
- Method name changed on line 12
- No other changes in the file
- Code still compiles

---

### Test 1.2: Extract Method - Analyze Phase (Kotlin)

**Operation:** Analyze code on lines 22-27 for extraction

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "startLine": 22,
    "endLine": 27
  }'
```

**Expected Response:**
```json
{
  "canExtract": true,
  "suggestedMethodName": "calculateOrderTotal",
  "detectedParameters": [
    {"name": "items", "type": "List<String>", "isOutput": false}
  ],
  "returnType": "Pair<Double, Double>",
  "suggestedVisibility": "private",
  "language": "Kotlin"
}
```

**Verification:**
- `canExtract` is true
- Detected `items` parameter
- Return type includes both `total` and `tax`
- No errors in response

---

### Test 1.3: Extract Method - Execute Phase (Kotlin)

**Operation:** Extract lines 22-27 into a new method using analysis results

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/execute \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "startLine": 22,
    "endLine": 27,
    "methodName": "calculateOrderTotal",
    "parameterOrder": ["items"],
    "visibility": "private",
    "isStatic": false
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

**Expected Code Change:**
```kotlin
// Lines 22-27: Before (extracted code)
var total = 0.0
for (item in items) {
    val price = getPrice(item)
    total += price
}
val tax = total * 0.1

// After (method call)
val calculationResult = calculateOrderTotal(items)
val total = calculationResult.first
val tax = calculationResult.second

// New method added:
private fun calculateOrderTotal(items: List<String>): Pair<Double, Double> {
    var total = 0.0
    for (item in items) {
        val price = getPrice(item)
        total += price
    }
    val tax = total * 0.1
    return Pair(total, tax)
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/kotlin/SampleClass_afterExtractMethod.kt`

**Verification:**
- New method `calculateOrderTotal` exists
- Original code replaced with method call
- Code still compiles and runs correctly
- Method signature matches return type `Pair<Double, Double>`

---

## Test Suite 2: JavaScript Refactoring

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js`

### Test 2.1: Rename Method (JavaScript)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "line": 11,
    "oldName": "oldMethodName",
    "newName": "newMethodName"
  }'
```

**Expected Code Change:**
```javascript
// Line 11: After
newMethodName(value) {
    return value * 2;
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/javascript/SampleClass_afterRename.js`

---

### Test 2.2: Extract Method - Analyze Phase (JavaScript)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "startLine": 23,
    "endLine": 28
  }'
```

**Expected Response:**
```json
{
  "canExtract": true,
  "suggestedMethodName": "calculateOrderTotal",
  "detectedParameters": [
    {"name": "items", "type": "Array", "isOutput": false}
  ],
  "returnType": "{total: number, tax: number}",
  "suggestedVisibility": "private",
  "language": "JavaScript"
}
```

---

### Test 2.3: Extract Method - Execute Phase (JavaScript)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/execute \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js",
    "startLine": 23,
    "endLine": 28,
    "methodName": "calculateOrderTotal",
    "parameterOrder": ["items"],
    "visibility": "private"
  }'
```

**Expected Code Change:**
```javascript
// After (method call)
const {total, tax} = this.calculateOrderTotal(items);

// New method:
calculateOrderTotal(items) {
    let total = 0.0;
    for (const item of items) {
        const price = this.getPrice(item);
        total += price;
    }
    const tax = total * 0.1;
    return {total, tax};
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/javascript/SampleClass_afterExtractMethod.js`

---

## Test Suite 3: TypeScript Refactoring

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts`

### Test 3.1: Rename Method (TypeScript)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "line": 11,
    "oldName": "oldMethodName",
    "newName": "newMethodName"
  }'
```

**Expected Code Change:**
```typescript
// Line 11: After
newMethodName(value: number): number {
    return value * 2;
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/typescript/SampleClass_afterRename.ts`

---

### Test 3.2: Extract Method - Analyze Phase (TypeScript)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "startLine": 23,
    "endLine": 28
  }'
```

**Expected Response:**
```json
{
  "canExtract": true,
  "suggestedMethodName": "calculateOrderTotal",
  "detectedParameters": [
    {"name": "items", "type": "string[]", "isOutput": false}
  ],
  "returnType": "{total: number, tax: number}",
  "suggestedVisibility": "private",
  "language": "TypeScript"
}
```

---

### Test 3.3: Extract Method - Execute Phase (TypeScript)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/execute \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts",
    "startLine": 23,
    "endLine": 28,
    "methodName": "calculateOrderTotal",
    "parameterOrder": ["items"],
    "visibility": "private"
  }'
```

**Expected Code Change:**
```typescript
// After (method call with types)
const {total, tax} = this.calculateOrderTotal(items);

// New method with full type annotations:
private calculateOrderTotal(items: string[]): {total: number, tax: number} {
    let total: number = 0.0;
    for (const item of items) {
        const price = this.getPrice(item);
        total += price;
    }
    const tax: number = total * 0.1;
    return {total, tax};
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/typescript/SampleClass_afterExtractMethod.ts`

---

## Test Suite 4: Scala Refactoring

### Test File
`src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala`

### Test 4.1: Rename Method (Scala)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "line": 13,
    "oldName": "oldMethodName",
    "newName": "newMethodName"
  }'
```

**Expected Code Change:**
```scala
// Line 13: After
def newMethodName(value: Int): Int = {
    value * 2
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/scala/SampleClass_afterRename.scala`

---

### Test 4.2: Extract Method - Analyze Phase (Scala)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "startLine": 25,
    "endLine": 30
  }'
```

**Expected Response:**
```json
{
  "canExtract": true,
  "suggestedMethodName": "calculateOrderTotal",
  "detectedParameters": [
    {"name": "items", "type": "List[String]", "isOutput": false}
  ],
  "returnType": "(Double, Double)",
  "suggestedVisibility": "private",
  "language": "Scala"
}
```

---

### Test 4.3: Extract Method - Execute Phase (Scala)

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/execute \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala",
    "startLine": 25,
    "endLine": 30,
    "methodName": "calculateOrderTotal",
    "parameterOrder": ["items"],
    "visibility": "private"
  }'
```

**Expected Code Change:**
```scala
// After (tuple destructuring)
val (total, tax) = calculateOrderTotal(items)

// New method:
private def calculateOrderTotal(items: List[String]): (Double, Double) = {
    var total = 0.0
    for (item <- items) {
      val price = getPrice(item)
      total += price
    }
    val tax = total * 0.1
    (total, tax)
}
```

**Full Expected File:** `src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/expected/scala/SampleClass_afterExtractMethod.scala`

---

## Verification Checklist

For each test:

- [ ] API call returns expected HTTP status code (200 for success)
- [ ] Response JSON matches expected structure
- [ ] Code changes match expected snippets
- [ ] Full file diff matches expected file
- [ ] Modified code compiles without errors
- [ ] No unintended side effects in other parts of the file
- [ ] Extracted methods have correct signatures (types, parameters, visibility)
- [ ] Original functionality is preserved (if possible to run)

---

## Cleanup After Testing

After completing all tests, revert the test files to their original state:

```bash
# Revert all test files
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/javascript/SampleClass.js
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/typescript/SampleClass.ts
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/scala/SampleClass.scala

# Or revert all at once
git checkout src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/*/SampleClass.*
```

---

## Error Scenarios to Test

### Test E.1: Unsupported Language

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/python/test.py",
    "startLine": 10,
    "endLine": 15
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": {
    "code": "LANGUAGE_NOT_SUPPORTED",
    "message": "Extract method refactoring is not supported for Python. Supported languages: Kotlin, JavaScript, TypeScript, Scala",
    "details": {
      "requestedLanguage": "Python",
      "supportedLanguages": ["Kotlin", "JavaScript", "TypeScript", "Scala"]
    }
  }
}
```

### Test E.2: Invalid Line Range

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/extract-method/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/test/kotlin/io/miiken/intellijcontrolserver/fixtures/kotlin/SampleClass.kt",
    "startLine": 100,
    "endLine": 105
  }'
```

**Expected Response:**
```json
{
  "canExtract": false,
  "errorMessage": "Invalid line range: lines 100-105 are outside the file bounds"
}
```

### Test E.3: File Not Found

**API Call:**
```bash
curl -X POST http://localhost:8765/intellij-control-server/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/nonexistent/file.kt",
    "line": 10,
    "oldName": "foo",
    "newName": "bar"
  }'
```

**Expected Response:**
```json
{
  "success": false,
  "error": {
    "code": "FILE_NOT_FOUND",
    "message": "File not found: src/nonexistent/file.kt"
  }
}
```

---

## Notes

- **Line Numbers**: Are 1-based (as shown in editors)
- **Column Numbers**: Are 0-based (if/when used)
- **File Paths**: Relative to project root
- **Project Name**: Must match the actual IntelliJ project name in the API URL
- **Timing**: Some refactorings may take a few seconds; allow up to 10 seconds for response
- **IDE State**: IntelliJ must have the project open and indexed for refactorings to work

---

## Success Criteria

All tests pass when:

1. All API calls return expected responses
2. All code changes match expected results
3. All modified code compiles
4. No errors in IntelliJ's logs
5. Files can be reverted cleanly with git checkout

---

## Test Execution Log

Use this section to track test execution:

| Test ID | Date | Status | Notes |
|---------|------|--------|-------|
| 1.1 Kotlin Rename | | ⏸️ | |
| 1.2 Kotlin Analyze | | ⏸️ | |
| 1.3 Kotlin Extract | | ⏸️ | |
| 2.1 JS Rename | | ⏸️ | |
| 2.2 JS Analyze | | ⏸️ | |
| 2.3 JS Extract | | ⏸️ | |
| 3.1 TS Rename | | ⏸️ | |
| 3.2 TS Analyze | | ⏸️ | |
| 3.3 TS Extract | | ⏸️ | |
| 4.1 Scala Rename | | ⏸️ | |
| 4.2 Scala Analyze | | ⏸️ | |
| 4.3 Scala Extract | | ⏸️ | |
| E.1 Unsupported Lang | | ⏸️ | |
| E.2 Invalid Range | | ⏸️ | |
| E.3 File Not Found | | ⏸️ | |
