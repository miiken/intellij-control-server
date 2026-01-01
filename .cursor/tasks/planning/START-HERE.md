# Start Here - IntelliJ Control Server

Welcome to the IntelliJ Control Server plugin development workspace!

## 📁 Project Structure

```
intellij-control-server/
├── README.md                           # Quick overview
├── build.gradle.kts                    # Build configuration
├── settings.gradle.kts                 # Gradle settings
├── gradle.properties                   # Gradle properties
├── .cursor/tasks/planning/             # Planning documents
│   ├── START-HERE.md                   # This file
│   ├── README.md                       # Full project overview
│   ├── ARCHITECTURE.md                 # Technical design
│   ├── API-SPEC.md                     # Complete API reference
│   ├── IMPLEMENTATION-PLAN.md          # Development roadmap
│   └── USE-CASES.md                    # Real-world examples
├── src/main/kotlin/                    # Source code (to be implemented)
│   └── io/hibob/intellijcontrolserver/
│       ├── server/                     # HTTP server
│       │   └── handlers/               # Request handlers
│       └── services/                   # Service layer
└── cli/                                # CLI tool (optional)
```

## 📚 Documentation Overview

### 1. [README.md](README.md)
**Purpose**: Quick project overview and getting started guide

**Read this for**:
- What the plugin does
- Key features overview
- Quick installation instructions
- Basic usage examples

### 2. [ARCHITECTURE.md](ARCHITECTURE.md)
**Purpose**: Technical architecture and design decisions

**Read this for**:
- System architecture diagrams
- Component responsibilities
- Threading model
- Data flow
- IntelliJ Platform API integration
- Security considerations

### 3. [API-SPEC.md](API-SPEC.md)
**Purpose**: Complete HTTP API reference

**Read this for**:
- All API endpoints
- Request/response formats
- Error codes
- Usage examples
- Testing endpoints with curl

### 4. [IMPLEMENTATION-PLAN.md](IMPLEMENTATION-PLAN.md) ⭐ **Start Here for Development**
**Purpose**: Development roadmap and implementation guide

**Read this for**:
- 8-week development phases
- Task breakdown
- Testing strategy
- Development environment setup
- Architecture decisions (why we chose X over Y)
- Risk management

### 5. [USE-CASES.md](USE-CASES.md)
**Purpose**: Real-world usage scenarios

**Read this for**:
- How to use with Cursor AI
- Automation scripts
- Integration examples
- Client library implementations

## 🚀 Getting Started

### For Development

1. **Read IMPLEMENTATION-PLAN.md** - Start here to understand the roadmap
2. **Set up environment**:
   ```bash
   # Install prerequisites
   - IntelliJ IDEA Ultimate (for plugin development)
   - JDK 17+
   - Gradle 8.0+
   
   # Build the plugin
   ./gradlew buildPlugin
   
   # Run in sandbox
   ./gradlew runIde
   ```

3. **Follow Phase 1** in IMPLEMENTATION-PLAN.md (Foundation)

### For Understanding the Architecture

1. **Read ARCHITECTURE.md** - Understand the design
2. **Read API-SPEC.md** - See what we're building
3. **Read USE-CASES.md** - See how it will be used

## 📋 Current Status

**Phase**: Planning Complete ✅
**Next**: Begin Phase 1 - Foundation (Week 1)

### Immediate Next Steps

1. ✅ Project structure created
2. ✅ Build configuration set up
3. ✅ Planning documents complete
4. ⏳ **Next**: Implement HTTP server (Phase 1)

## 🎯 Development Priorities

### Phase 1 (Week 1): Foundation
- [ ] Basic HTTP server
- [ ] Health check endpoint
- [ ] Configuration loading
- [ ] Logging infrastructure

### Phase 2 (Week 2): Tasks API
- [ ] Task listing
- [ ] Task switching
- [ ] Context save/restore

### Phase 3 (Week 3-4): Basic Refactoring
- [ ] Rename refactoring

See [IMPLEMENTATION-PLAN.md](IMPLEMENTATION-PLAN.md) for complete roadmap.

## 💡 Key Concepts

### What This Plugin Does

Runs a **local HTTP server** inside IntelliJ IDEA that exposes IDE operations:

```
External Client (Cursor, CLI, Script)
    ↓ HTTP/JSON
IntelliJ Control Server Plugin
    ↓ IntelliJ Platform APIs
IntelliJ IDEA (Tasks, Refactoring, Navigation)
```

### Why This is Useful

- **AI Integration**: Let Cursor AI directly control IntelliJ refactoring
- **Automation**: Script repetitive tasks
- **No Context Switching**: Stay in Cursor while IntelliJ does the heavy lifting
- **Type-Safe**: Leverage IntelliJ's powerful refactoring engine

### Core Operations

1. **Tasks & Context**: Manage IntelliJ tasks, save/restore editor state
2. **Refactoring**: Rename, extract method, move class, etc.
3. **Navigation**: Open files, jump to definition, find usages

## 🔧 Development Workflow

```bash
# 1. Make changes in src/main/kotlin
vim src/main/kotlin/io/hibob/intellijcontrolserver/ControlServer.kt

# 2. Run tests
./gradlew test

# 3. Test in sandbox IntelliJ
./gradlew runIde
# (Opens new IntelliJ instance with plugin installed)

# 4. Test API
curl http://localhost:8765/health

# 5. Build plugin
./gradlew buildPlugin
# Output: build/distributions/intellij-control-server-1.0.0.zip

# 6. Install in real IntelliJ
# Settings → Plugins → ⚙️ → Install Plugin from Disk
```

## 📖 Recommended Reading Order

**For Implementation**:
1. IMPLEMENTATION-PLAN.md (understand roadmap)
2. ARCHITECTURE.md (understand design)
3. API-SPEC.md (understand what to build)

**For Usage/Integration**:
1. README.md (quick overview)
2. API-SPEC.md (how to call the API)
3. USE-CASES.md (real-world examples)

## 🤝 Contributing

See IMPLEMENTATION-PLAN.md → Contributing Guidelines section

## ❓ Questions?

Check the relevant planning document:
- "How does it work?" → ARCHITECTURE.md
- "What endpoints exist?" → API-SPEC.md
- "How do I implement X?" → IMPLEMENTATION-PLAN.md
- "How would I use this?" → USE-CASES.md

---

**Ready to start developing?** → Begin with [IMPLEMENTATION-PLAN.md](IMPLEMENTATION-PLAN.md) Phase 1! 🚀

