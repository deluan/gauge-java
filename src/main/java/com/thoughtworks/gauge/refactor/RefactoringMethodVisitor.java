// Copyright 2015 ThoughtWorks, Inc.

// This file is part of Gauge-Java.

// This program is free software.
//
// It is dual-licensed under:
// 1) the GNU General Public License as published by the Free Software Foundation,
// either version 3 of the License, or (at your option) any later version;
// or
// 2) the Eclipse Public License v1.0.
//
// You can redistribute it and/or modify it under the terms of either license.
// We would then provide copied of each license in a separate .txt file with the name of the license as the title of the file.

package com.thoughtworks.gauge.refactor;

import com.thoughtworks.gauge.StepValue;
import gauge.messages.Messages;
import org.apache.commons.lang.StringEscapeUtils;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.expr.AnnotationExpr;
import org.walkmod.javalang.ast.expr.BinaryExpr;
import org.walkmod.javalang.ast.expr.SingleMemberAnnotationExpr;
import org.walkmod.javalang.ast.expr.StringLiteralExpr;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;

public class RefactoringMethodVisitor extends VoidVisitorAdapter {
    private StepValue oldStepValue;
    private StepValue newStepValue;
    private List<Messages.ParameterPosition> paramPositions;
    private boolean refactored;
    private JavaRefactoringElement javaElement;


    public RefactoringMethodVisitor(StepValue oldStepValue, StepValue newStepValue, List<Messages.ParameterPosition> paramPositions) {
        this.oldStepValue = oldStepValue;
        this.newStepValue = newStepValue;
        this.paramPositions = paramPositions;
    }


    public void visit(MethodDeclaration methodDeclaration, Object arg) {
        try {
            List<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
            if (annotations == null)
                return;
            for (AnnotationExpr annotationExpr : annotations) {
                if (!(annotationExpr instanceof SingleMemberAnnotationExpr))
                    continue;

                SingleMemberAnnotationExpr annotation = (SingleMemberAnnotationExpr) annotationExpr;
                if (annotation.getMemberValue() instanceof BinaryExpr) {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
                    try {
                        Object result = engine.eval(annotation.getMemberValue().toString());
                        refactor(methodDeclaration, new StringLiteralExpr(result.toString()), annotation);
                    } catch (ScriptException e) {
                        continue;
                    }
                }
                if (annotation.getMemberValue() instanceof StringLiteralExpr) {
                    StringLiteralExpr memberValue = (StringLiteralExpr) annotation.getMemberValue();
                    refactor(methodDeclaration, memberValue, annotation);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void refactor(MethodDeclaration methodDeclaration, StringLiteralExpr memberValue, SingleMemberAnnotationExpr annotation) {
        if (StringEscapeUtils.unescapeJava(memberValue.getValue()).trim().equals(oldStepValue.getStepAnnotationText().trim())) {
            List<Parameter> newParameters = Arrays.asList(new Parameter[paramPositions.size()]);
            memberValue.setValue(newStepValue.getStepAnnotationText());
            List<Parameter> parameters = methodDeclaration.getParameters();
            for (int i = 0, paramPositionsSize = paramPositions.size(); i < paramPositionsSize; i++) {
                if (paramPositions.get(i).getOldPosition() < 0)
                    newParameters.set(paramPositions.get(i).getNewPosition(), new Parameter(new ClassOrInterfaceType("String"), new VariableDeclaratorId(Util.convertToCamelCase(newStepValue.getParameters().get(i)))));
                else
                    newParameters.set(paramPositions.get(i).getNewPosition(), parameters.get(paramPositions.get(i).getOldPosition()));
            }
            methodDeclaration.setParameters(newParameters);
            annotation.setMemberValue(memberValue);
            this.javaElement = new JavaRefactoringElement(methodDeclaration.getBeginLine(), methodDeclaration.getEndLine(), methodDeclaration.getBeginColumn() - 1, StringEscapeUtils.unescapeJava(methodDeclaration.toString()), null);
            this.refactored = true;
        }
    }

    public boolean refactored() {
        return this.refactored;
    }

    public JavaRefactoringElement getRefactoredJavaElement() {
        return this.javaElement;
    }
}
