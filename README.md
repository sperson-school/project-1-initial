# Project 1 - Get to the Point

###### Objectives
This assignment is designed to get you comfortable with a modern developer workflow that includes AI-assisted coding. You will use an AI tool (Claude is the best one for this assignment) to help you develop a Java class, and you will learn to verify the AI's output using provided unit tests.

###### Concepts Covered
* Review of basic Java concepts
* Java class design
* Java records
* AI-assisted coding
* Unit testing in Java

###### Setup Details
* Review Java programming basics paying close attention to creating custom classes and records.
* Fork the project template from https://github.com/UltimateSandbox/project-1-initial.git
* Clone your new project from your own GitHub repository to your local machine.

###### Assignment Overview
1. Utilizing the given starting template, complete the following programming assignment to create a `Point` class that represents a point in the Cartesian plane. You will use AI to help generate the code for this class, and you will verify its correctness using unit tests.
* **Review the Class Design:** Carefully inspect the informal UML and the list of requirements below for the `Point` class. This is your chance to understand the requirements before you start coding.
* **Prompt an AI:** Craft a clear and specific prompt for a code generation AI. Your goal is to get the AI to generate the complete `Point` class. Be sure to include all the requirements, such as the class name and required methods.
* **Verify with Unit Tests:** Use the provided unit tests to check the AI's work. Run the tests with no modifications. If any tests fail, it means the AI's code is incorrect.
* **Debug the AI's Code:** If the tests fail, your task is to debug and fix the AI-generated code. This is a critical skill for working with AI in a professional setting. Do not simply ask the AI to try again; you must understand and correct the errors yourself sometimes.

2. Once you have a working `Point` class, ask the AI to generate a `PointRecord` class that implements the same functionality as the `Point` class but uses Java's record feature. This will help you understand how records can simplify class design in Java.
* Call the new record `PointRecord`
* Copy the record code into the same package as the `Point` class.
* Debug if necessary, using the given unit tests to verify correctness.

3. **Answer the Project Questions:** After completing the coding tasks, answer the questions in the provided `ANSWERME.md` file. These questions will help you reflect on your experience with AI-assisted coding and the project requirements.
###### Requirements for the Point Class
* A constructor that takes x and y coordinates as double arguments
* `setX(double x)`, `setY(double y)` - standard setters
* `getX()`, `getY()` - standard getters
* `setPoint(double x, double y)` - set the coordinates of the point.
* `shiftX(double n)`, `shiftY(double n)` - shift a point by a given amount along one of the axes.
* `distance(Point p2)` - finds the distance to point p2.
* `rotate(double angle)` - rotates the point by a specified (radian) angle around the origin
* Include a `toString` method as well. If you're not sure what the toString method does, look it up.  It's a very useful method to have in your classes.
* Any other methods you believe to be necessary.  Feel free to add any additional methods that you think would be useful for the Point class, but make sure they are relevant and enhance the functionality of the class.

![img.png](img.png)

###### Notes
* Your code should work with the unit tests provided.
* Use the tests as-is to test your class for correctness.

###### Expected Output
Once you get your `Point` class compiling, when you run the unit tests, you should see all tests pass.

###### Helpful Hints
- Make good use of whitespace and comments to make your implementation as clean as possible.  If necessary, clean up the AI generated code.
- Use good, SOLID object-oriented programming principles (pun intended) in your implementation.

###### Deliverables
- Be sure you commit & push your code to GitHub.  If you don't push it, I won't be able to see it!
- Make sure your repo is **public**, or I won't be able to see it.
- Copy the URL for your repo (green button on your GitHub repos' page) and paste it into the Website URL field in Canvas and click Submit Assignment!
