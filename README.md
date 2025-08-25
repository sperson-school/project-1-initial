# Project 1 - Let's Get to the Point (and Line and Cube)

## Project Objectives

This project introduces students to advanced object-oriented programming concepts through the implementation of 3D geometric classes while exploring the strategic use of AI code generation tools. Students will learn to:

1. **Master Object Composition**: Understand how complex data structures are built from simpler components (Cube3D composed of Point3D and Line3D objects)
2. **Apply Design Patterns**: Implement fundamental patterns including Immutability, Factory Method, Composite, and Value Object patterns
3. **Practice Defensive Programming**: Develop robust code with comprehensive validation, error handling, and logging
4. **Utilize AI Tools Effectively**: Learn to craft precise prompts for AI code generation while maintaining critical evaluation of generated code
5. **Understand 3D Mathematics**: Apply geometric algorithms for rotation, translation, distance calculations, and vector operations
6. **Implement Immutable Design**: Create thread-safe, predictable objects following functional programming principles

## Concepts Covered

### Core Object-Oriented Design Patterns
- **Immutability Pattern**: All classes follow immutable design principles for thread safety and predictable behavior
- **Composite Pattern**: Cube3D demonstrates complex objects composed of simpler components
- **Factory Method Pattern**: Multiple constructors provide flexible object creation approaches
- **Value Object Pattern**: Proper implementation of equals(), hashCode(), and toString() methods
- **Defensive Programming Pattern**: Comprehensive null checking and input validation

### Advanced Java Concepts
- **Java Logging Framework**: Structured logging using java.util.logging with INFO, WARNING, and SEVERE levels
- **Generic Collections**: Use of ArrayList and other collection classes for managing object relationships
- **Exception Handling**: Proper use of IllegalArgumentException and IllegalStateException
- **Method Chaining**: Fluent interfaces for mathematical operations

### 3D Mathematics and Algorithms
- **Vector Mathematics**: Dot product, cross product, magnitude, and normalization operations
- **3D Transformations**: Rotation matrices for X, Y, and Z axis rotations
- **Distance Algorithms**: Euclidean distance calculations between points, lines, and geometric shapes
- **Geometric Validation**: Algorithms to verify structural integrity of 3D objects

### Data Structure Foundations
- **Object Aggregation**: Understanding "has-a" vs "is-a" relationships
- **Array Management**: Defensive copying and bounds checking for internal arrays
- **Collection Management**: Working with lists of related objects (vertices, edges)

## Setup Details

- **Fork** the project template from https://github.com/UltimateSandbox/project-1-initial.git
- **Clone** your new project from your own GitHub repository to your local machine.

## Assignment Overview

This assignment demonstrates the strategic use of AI code generation tools while maintaining critical thinking and code analysis skills. Students will generate three interconnected classes using carefully crafted prompts, then analyze and understand the generated code.

### Part 1: Point3D Class Generation

**Objective**: Create a foundational 3D point class with mathematical operations.

**AI Generation Prompt**:
```
Would you generate a class in Java that represents a point in 3D space. Include methods for distance, as well as rotation and any other methods that make sense for a 3D point. Include comments for each method in the style of Spring Framework getting started guides explaining each method in detail. Also include a logger utilizing the Java util logging framework. Use log lines to print information and errors in each method that make sense for the method context. For now, just utilize the INFO, WARNING and SEVERE log levels. Document the object-oriented design patterns used in the class and explain how they demonstrate foundational principles for data structures and algorithms.
```

**Critical Analysis Tasks**:
- Identify all design patterns implemented in the generated code
- Explain why immutability was chosen for this geometric class
- Analyze the mathematical correctness of rotation methods
- Evaluate the logging strategy and suggest improvements

### Part 2: Line3D Class Generation

**Objective**: Build upon Point3D to create a line segment class demonstrating object composition.

**AI Generation Prompt**:
```
Would you generate a class in Java that represents a line in 3D space. Utilize the Point3D class we created earlier as necessary. Include methods for line length, as well as a shortest distance between lines method and any other methods that make sense for a 3D line. Include comments for each method in the style of Spring Framework getting started guides explaining each method in detail. Also include a logger utilizing the Java util logging framework. Use log lines to print information and errors in each method that make sense for the method context. For now, just utilize the INFO, WARNING and SEVERE log levels. Document the object-oriented design patterns used in the class and explain how they demonstrate foundational principles for data structures and algorithms.
```

**Critical Analysis Tasks**:
- Examine how Line3D demonstrates the Composition pattern with Point3D
- Analyze the complexity of the shortest distance algorithm between skew lines
- Evaluate the mathematical accuracy of parallel line detection using cross products
- Assess the defensive programming techniques used in method parameters

### Part 3: Cube3D Class Generation

**Objective**: Create a complex 3D shape class that demonstrates advanced object composition and geometric algorithms.

**AI Generation Prompt**:
```
Would you generate a class in Java that represents a cube in 3D space. Utilize the Point3D class as well as the Line3D class we created earlier as necessary. Include methods for cube rotation, translation and perimeter length as well as volume and any other methods that make sense for a 3D cube that may be used in a 3D graphics program. Include comments for each method in the style of Spring Framework getting started guides explaining each method in detail. Also include a logger utilizing the Java util logging framework. Use log lines to print information and errors in each method that make sense for the method context. For now, just utilize the INFO, WARNING and SEVERE log levels. Document the object-oriented design patterns used in the class and explain how they demonstrate foundational principles for data structures and algorithms.
```

**Critical Analysis Tasks**:
- Analyze how Cube3D demonstrates multiple design patterns simultaneously
- Evaluate the cube validation algorithm in `isValidCube()`
- Examine the edge generation logic and verify the cube topology
- Assess the transformation methods (rotation, translation, scaling) for correctness

## Learning Outcomes Assessment

### AI Tool Usage Evaluation
Students should demonstrate:
1. **Prompt Engineering Skills**: Ability to craft clear, specific prompts that generate desired functionality
2. **Critical Code Review**: Systematic analysis of generated code for correctness and efficiency
3. **Pattern Recognition**: Identification of design patterns and architectural decisions in generated code
4. **Mathematical Verification**: Validation of geometric algorithms and mathematical operations

### Technical Mastery Indicators
- Understanding of immutable object design and its benefits
- Ability to trace object composition relationships across all three classes
- Comprehension of 3D geometric algorithms and their implementation
- Recognition of defensive programming practices and their importance

### Advanced Concepts Integration
- Connection between geometric transformations and linear algebra concepts
- Understanding of logging as a debugging and monitoring strategy
- Appreciation for the relationship between object-oriented design and data structure efficiency
- Recognition of how these patterns scale to more complex systems

## Extension Opportunities

For advanced students or additional exploration:
1. **Performance Analysis**: Compare different approaches to geometric calculations
2. **Visualization Integration**: Connect these classes to a 3D graphics library
3. **Serialization**: Add JSON or XML serialization capabilities
4. **Unit Testing**: Create comprehensive test suites for all mathematical operations
5. **Additional Shapes**: Generate classes for other 3D primitives (Sphere, Cylinder, etc.)

## Best Practices for AI-Assisted Development

### Effective Prompt Strategies
- **Be Specific**: Include exact requirements for logging, comments, and design patterns
- **Provide Context**: Reference previously created classes for consistency
- **Request Documentation**: Ask for detailed explanations of design decisions
- **Specify Standards**: Request adherence to specific coding conventions

### Critical Evaluation Guidelines
- **Test All Edge Cases**: Verify behavior with null inputs, zero values, and extreme coordinates
- **Validate Mathematical Operations**: Manually check key geometric calculations
- **Assess Design Coherence**: Ensure consistent patterns across related classes
- **Review for Maintainability**: Evaluate code clarity and extensibility

### Helpful Hints
- If necessary, clean up the AI generated code.
- Ensure the use of good, SOLID object-oriented programming principles (pun intended) in your implementation.

### Deliverables
- Be sure you **commit & push** your code to GitHub.  If you don't push it, I won't be able to see it!
- Make sure your repo is **public**, or I won't be able to see it.
- Copy the URL for your repo (green button on your GitHub repos' page) and paste it into the Website URL field in Canvas and click Submit Assignment!

### Conclusion
This project demonstrates that AI tools can significantly accelerate development while requiring critical thinking skills to ensure correctness, maintainability, and educational value.