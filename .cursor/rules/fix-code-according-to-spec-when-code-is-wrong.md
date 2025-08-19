---
alwaysApply: true
---

# Fix Code According to Specification When Code is Wrong

## Purpose
This rule ensures that when code implementation doesn't match the functional specification due to **wrong implementation** (not missing features), the code must be corrected to align with the specification. This maintains specification-driven development integrity.

## Critical Distinction

### ❌ **Wrong Implementation** - MUST FIX IMMEDIATELY
- Code exists but implements different behavior than specified
- UI layout differs from specification requirements  
- Data flow contradicts specified workflow
- User interaction patterns don't match specification
- **Example**: Spec requires dropdown selection, code uses text input

### ⏳ **Not Implemented Yet** - ACCEPTABLE (Future Work)
- Features mentioned in spec but not yet built
- Placeholder implementations with TODO comments
- Future functionality clearly marked as coming later
- **Example**: Spec mentions Frame 3, but only Frame 1-2 exist

## When to Apply This Rule

### 🚨 **IMMEDIATE ACTION REQUIRED**
- Existing code contradicts functional specification
- UI components implement wrong interaction patterns
- Data models don't match specified structure
- Workflow deviates from specified user flow
- Navigation doesn't follow specified paths

### ✅ **NO ACTION NEEDED**  
- Features not yet implemented (marked as TODO/Future)
- Placeholder implementations that acknowledge future requirements
- Simplified MVP versions that maintain spec intent
- Features explicitly marked as "Phase 2" or "Future"

## Required Actions

### 1. **Identify Wrong Implementation**
- Compare existing code behavior against \`spec/specification.md\`
- Identify specific components that contradict specification
- Document the mismatch clearly (what is vs what should be)
- Distinguish between wrong implementation vs missing features

### 2. **Assess Impact and Priority**
- **HIGH PRIORITY**: Core user workflows that contradict spec
- **MEDIUM PRIORITY**: UI/UX elements that don't match specified behavior
- **LOW PRIORITY**: Data structure mismatches that don't affect user experience

### 3. **Fix Implementation**
- Update code to match specification requirements exactly  
- Refactor UI components to specified interaction patterns
- Modify data models to align with specified structure
- Update navigation flows to match specified paths
- Maintain backward compatibility where possible

### 4. **Update Documentation**
- Mark the wrong implementation as fixed in \`spec/dev_spec_and_status.md\`
- Update progress tracking to reflect corrected implementation
- Add notes about what was changed and why

### 5. **Validate Alignment**
- Test that corrected implementation matches specification behavior
- Verify user workflows follow specified patterns
- Confirm UI matches specified layout and interactions
- Ensure data flow aligns with specified requirements

## Example Scenarios

### **Scenario 1: UI Pattern Mismatch**
- **Specification**: "Dropdown to select teams from league roster"
- **Wrong Implementation**: Text input fields for team names
- **Action**: Replace text inputs with dropdown/spinner components
- **Priority**: HIGH (core workflow contradiction)

### **Scenario 2: Data Structure Mismatch** 
- **Specification**: "12 players per team with predefined rosters"
- **Wrong Implementation**: Manual player entry with no team structure
- **Action**: Create Team and TeamPlayer models, refactor to use predefined data
- **Priority**: HIGH (fundamental data model contradiction)

## Success Criteria

### ✅ **Specification Alignment Achieved When:**
- All existing code behavior matches specification requirements
- User workflows follow specified patterns exactly
- UI components implement specified interaction methods
- Data models align with specified structure
- No contradictions exist between code and specification

**Specification is the source of truth** - code must conform to spec, not vice versa.
