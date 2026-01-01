# Use Cases

Real-world scenarios demonstrating how to use IntelliJ Control Server.

---

## Use Case 1: AI-Assisted Refactoring with Cursor

**Scenario**: Developer is using Cursor AI to refactor code. Cursor analyzes the code and suggests renaming `Service` to `EmployeeService` across the codebase.

**Before (Manual Process)**:
1. Developer reads Cursor's suggestion
2. Opens IntelliJ
3. Manually navigates to class
4. Right-click → Refactor → Rename
5. Enters new name
6. Reviews changes
7. Confirms refactoring
8. Returns to Cursor

**After (With Control Server)**:

Cursor executes directly:
```bash
curl -X POST http://localhost:8765/refactor/rename \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "src/main/kotlin/Service.kt",
    "offset": 150,
    "newName": "EmployeeService"
  }'
```

Response confirms refactoring:
```json
{
  "success": true,
  "filesChanged": ["Service.kt", "Controller.kt", "ServiceTest.kt"],
  "changesCount": 15
}
```

**Benefits**:
- Seamless workflow (no context switching)
- AI can perform multi-step refactoring
- Faster iteration

---

## Use Case 2: Task Management Integration

**Scenario**: Developer receives Asana task, wants to create matching IntelliJ task and switch context.

**Cursor Script**:
```bash
#!/bin/bash
# When user says "work on Asana task #12345"

# 1. Fetch Asana task details
ASANA_TASK=$(asana_get_task "12345")
TASK_NAME=$(echo $ASANA_TASK | jq -r '.name')

# 2. Create IntelliJ task
curl -X POST http://localhost:8765/tasks/create \
  -d "{\"summary\":\"$TASK_NAME\"}"

# 3. Switch to new task (restores relevant files)
curl -X POST http://localhost:8765/tasks/switch \
  -d "{\"taskId\":\"$(echo $TASK_NAME | tr ' ' '-')\"}"

# 4. Cursor is now aware of task context
echo "Switched to task: $TASK_NAME"
```

**Result**: IntelliJ automatically opens relevant files for the task.

---

## Use Case 3: Automated Code Cleanup

**Scenario**: Clean up codebase by extracting long methods.

**Script (Python)**:
```python
import requests
import ast

BASE_URL = "http://localhost:8765"

# Find all methods longer than 50 lines
long_methods = find_long_methods("src/main/kotlin")

for method in long_methods:
    # Ask Cursor AI to analyze and suggest extraction
    suggestion = cursor_ai.analyze_method(method)
    
    if suggestion.should_extract:
        # Perform extraction via IntelliJ
        response = requests.post(f"{BASE_URL}/refactor/extract-method", json={
            "filePath": method.file,
            "startLine": suggestion.start_line,
            "endLine": suggestion.end_line,
            "methodName": suggestion.new_method_name
        })
        
        if response.json()["success"]:
            print(f"Extracted {suggestion.new_method_name} from {method.name}")
```

**Benefits**:
- Automate repetitive refactoring
- Maintain consistency across codebase
- AI-guided decisions with IDE execution

---

## Use Case 4: Cross-File Analysis and Refactoring

**Scenario**: Cursor identifies that a class should be moved to a different package based on dependency analysis.

**Cursor Workflow**:

1. **Analyze dependencies**:
```python
# Cursor analyzes codebase
dependencies = analyze_package_dependencies()

# Identifies: EmployeeService should be in 'employee' package
# Currently in 'service' package
```

2. **Execute move refactoring**:
```bash
curl -X POST http://localhost:8765/refactor/move-class \
  -d '{
    "filePath": "src/main/kotlin/service/EmployeeService.kt",
    "targetPackage": "com.miiken.employee.service"
  }'
```

3. **Verify**:
```json
{
  "success": true,
  "oldPath": "src/main/kotlin/service/EmployeeService.kt",
  "newPath": "src/main/kotlin/employee/service/EmployeeService.kt",
  "filesChanged": ["EmployeeService.kt", "Controller.kt", "..."],
  "importsUpdated": 12
}
```

**Result**: IntelliJ handles all import updates, reference updates across files.

---

## Use Case 5: Context-Aware Navigation

**Scenario**: Cursor is analyzing an error log and wants to jump to the relevant code.

**Error Log**:
```
Exception in EmployeeService.processEmployee line 42
NullPointerException at validateStatus
```

**Cursor Action**:
```bash
# Open file at specific line
curl -X POST http://localhost:8765/navigation/open-file \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "line": 42
  }'

# Find all usages of validateStatus
curl -X POST http://localhost:8765/navigation/find-usages \
  -d '{
    "filePath": "src/main/kotlin/EmployeeService.kt",
    "offset": 650
  }'
```

**Response**:
```json
{
  "success": true,
  "usages": [
    {
      "filePath": "src/main/kotlin/EmployeeService.kt",
      "line": 42,
      "snippet": "validateStatus(employee)"
    },
    {
      "filePath": "src/main/kotlin/EmployeeValidator.kt",
      "line": 15,
      "snippet": "service.validateStatus(emp)"
    }
  ]
}
```

**Result**: Cursor has full context to analyze and suggest fix.

---

## Use Case 6: Batch Refactoring Script

**Scenario**: Rename all "Dto" classes to "Request" across the codebase.

**Bash Script**:
```bash
#!/bin/bash
# batch-rename-dtos.sh

# Find all *Dto.kt files
find src/main/kotlin -name "*Dto.kt" | while read file; do
  # Extract class name
  classname=$(basename "$file" .kt)
  newname="${classname/Dto/Request}"
  
  echo "Renaming $classname to $newname in $file"
  
  # Perform rename via IntelliJ API
  curl -X POST http://localhost:8765/refactor/rename \
    -H "Content-Type: application/json" \
    -d "{
      \"filePath\": \"$file\",
      \"line\": 1,
      \"column\": 7,
      \"newName\": \"$newname\"
    }" | jq '.success'
  
  sleep 1  # Rate limiting
done

echo "Batch rename complete!"
```

**Result**: All DTOs renamed consistently, all references updated.

---

## Use Case 7: Documentation-Driven Development

**Scenario**: Generate documentation by querying IDE state.

**Python Script**:
```python
import requests
import json

BASE_URL = "http://localhost:8765"

# Get project structure
structure = requests.get(f"{BASE_URL}/navigation/project-structure").json()

# Generate markdown documentation
markdown = "# Project Structure\n\n"

for package, contents in structure["structure"].items():
    markdown += f"## Package: {package}\n\n"
    
    for file in contents["files"]:
        # Open each file and analyze
        filepath = f"{package}/{file}"
        
        # Could extend API to get class/method signatures
        # For now, document file existence
        markdown += f"- `{file}`\n"

# Save to README
with open("PROJECT-STRUCTURE.md", "w") as f:
    f.write(markdown)

print("Documentation generated!")
```

---

## Use Case 8: Test-Driven Refactoring

**Scenario**: Cursor identifies code that violates SOLID principles and suggests refactoring.

**Cursor Workflow**:

1. **Identify violation**:
```python
# Cursor analyzes: EmployeeService has too many responsibilities
# Suggests: Extract PayrollCalculationService
```

2. **Create new class file**:
```bash
# Cursor creates new file (via IDE file operations)
curl -X POST http://localhost:8765/refactor/extract-class \
  -d '{
    "sourceFile": "EmployeeService.kt",
    "methods": ["calculateSalary", "calculateBonus"],
    "newClassName": "PayrollCalculationService"
  }'
```

3. **Update tests**:
```bash
# Update test to use new service
curl -X POST http://localhost:8765/refactor/inline \
  -d '{
    "filePath": "EmployeeServiceTest.kt",
    "offset": 200
  }'
```

**Result**: Code is refactored following best practices, tests updated automatically.

---

## Use Case 9: Real-Time Collaboration with AI

**Scenario**: Multiple developers + AI assistant working on same codebase.

**Setup**:
- Developer A working on feature-branch-A
- Developer B working on feature-branch-B  
- Cursor monitoring both

**Cursor Script**:
```bash
# Monitor Developer A's task
A_TASK=$(curl http://localhost:8765/tasks/current | jq -r '.task.id')

# If Developer B's changes conflict with A's task
if conflicts_detected; then
  # Notify Developer A
  echo "Potential merge conflict detected"
  
  # Show diff between branches
  # (extend API to support git operations)
fi
```

**Future Enhancement**: WebSocket support for real-time notifications.

---

## Use Case 10: Code Review Automation

**Scenario**: Automated code review suggesting refactorings.

**GitHub Action + Cursor**:
```yaml
name: AI Code Review

on: [pull_request]

jobs:
  review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Start IntelliJ in headless mode
        run: |
          # Start IntelliJ with Control Server plugin
          intellij-headless --enable-control-server
      
      - name: Run Cursor analysis
        run: |
          # Cursor analyzes PR changes
          cursor analyze-pr --pr-number ${{ github.event.pull_request.number }}
      
      - name: Apply suggested refactorings
        run: |
          # Cursor applies safe refactorings via Control Server
          cursor apply-refactorings --auto-safe
      
      - name: Commit suggestions
        run: |
          git commit -am "AI-suggested refactorings"
          git push
```

**Result**: Automated, IDE-quality refactoring suggestions on every PR.

---

## Benefits Summary

### For Developers
- ✅ Faster refactoring (no context switching)
- ✅ AI-assisted code improvements
- ✅ Automated repetitive tasks
- ✅ Better task management

### For Teams
- ✅ Consistent code quality
- ✅ Automated code reviews
- ✅ Better documentation
- ✅ Knowledge sharing (AI learns patterns)

### For AI Tools (Cursor, etc.)
- ✅ Direct IDE control (better than text manipulation)
- ✅ Leverage IntelliJ's refactoring engine
- ✅ Type-safe refactoring
- ✅ Multi-file operations

---

## Client Library Examples

### Bash/curl (Simple)
```bash
alias ij='curl -s http://localhost:8765'
ij-tasks() { ij /tasks/list | jq; }
ij-rename() { ij -X POST /refactor/rename -d "{\"filePath\":\"$1\",\"offset\":$2,\"newName\":\"$3\"}"; }
```

### Python (requests)
```python
class IntelliJClient:
    def __init__(self, base_url="http://localhost:8765"):
        self.base_url = base_url
    
    def rename(self, file_path, offset, new_name):
        return requests.post(f"{self.base_url}/refactor/rename", json={
            "filePath": file_path,
            "offset": offset,
            "newName": new_name
        }).json()
```

### Node.js (axios)
```javascript
const axios = require('axios');

class IntelliJClient {
  constructor(baseURL = 'http://localhost:8765') {
    this.client = axios.create({ baseURL });
  }
  
  async rename(filePath, offset, newName) {
    const response = await this.client.post('/refactor/rename', {
      filePath, offset, newName
    });
    return response.data;
  }
}
```

---

## Future Use Cases

### With WebSocket Support (v2.0)
- Real-time code change notifications
- Live collaboration features
- Build/test status streaming

### With Extended API
- Git operations (commit, branch, merge)
- Debugging control (breakpoints, stepping)
- Build system integration
- Code generation from templates

