scope({c0_Enum:5, c0_Operator:6, c0_Security:5, c0_description:6, c0_name:6, c0_security:6});
defaultScope(1);
intRange(-8, 7);
stringLength(187);

c0_Enum = Abstract("c0_Enum");
c0_Security = Abstract("c0_Security");
c0_NoSecurity = Clafer("c0_NoSecurity").withCard(1, 1);
c0_Broken = Clafer("c0_Broken").withCard(1, 1);
c0_Weak = Clafer("c0_Weak").withCard(1, 1);
c0_Medium = Clafer("c0_Medium").withCard(1, 1);
c0_Strong = Clafer("c0_Strong").withCard(1, 1);
c0_Operator = Abstract("c0_Operator");
c0_name = c0_Operator.addChild("c0_name").withCard(1, 1);
c0_description = c0_Operator.addChild("c0_description").withCard(1, 1);
c0_security = c0_Operator.addChild("c0_security").withCard(1, 1);
c0_Task = Abstract("c0_Task");
c1_description = c0_Task.addChild("c1_description").withCard(1, 1);
c0_NONE = Clafer("c0_NONE").withCard(1, 1);
c0_OR = Clafer("c0_OR").withCard(1, 1);
c0_AND = Clafer("c0_AND").withCard(1, 1);
c0_NOT = Clafer("c0_NOT").withCard(1, 1);
c0_wFUSION = Clafer("c0_wFUSION").withCard(1, 1);
c0_cFUSION = Clafer("c0_cFUSION").withCard(1, 1);
c0_CertainTrust = Clafer("c0_CertainTrust").withCard(1, 1);
c0_op = c0_CertainTrust.addChild("c0_op").withCard(1, 1);
c1_security = c0_CertainTrust.addChild("c1_security").withCard(1, 1);
c0_Security.extending(c0_Enum).refToUnique(Int);
c0_NoSecurity.extending(c0_Security);
Constraint(implies(some(global(c0_NoSecurity)), equal(joinRef(global(c0_NoSecurity)), constant(0))));
c0_Broken.extending(c0_Security);
Constraint(implies(some(global(c0_Broken)), equal(joinRef(global(c0_Broken)), constant(1))));
c0_Weak.extending(c0_Security);
Constraint(implies(some(global(c0_Weak)), equal(joinRef(global(c0_Weak)), constant(2))));
c0_Medium.extending(c0_Security);
Constraint(implies(some(global(c0_Medium)), equal(joinRef(global(c0_Medium)), constant(3))));
c0_Strong.extending(c0_Security);
Constraint(implies(some(global(c0_Strong)), equal(joinRef(global(c0_Strong)), constant(4))));
c0_name.refToUnique(string);
c0_description.refToUnique(string);
c0_security.refToUnique(c0_Security);
c1_description.refToUnique(string);
c0_NONE.extending(c0_Operator);
c0_NONE.addConstraint(equal(joinRef(join($this(), c0_name)), constant("\"NONE\"")));
c0_NONE.addConstraint(equal(joinRef(join($this(), c0_description)), constant("\"The operator NONE is do nothing.\"")));
c0_NONE.addConstraint(equal(joinRef(join($this(), c0_security)), global(c0_Medium)));
c0_OR.extending(c0_Operator);
c0_OR.addConstraint(equal(joinRef(join($this(), c0_name)), constant("\"OR\"")));
c0_OR.addConstraint(equal(joinRef(join($this(), c0_description)), constant("\"The operator OR is applicable when opinions for two independent propositions form a new opinion reflecting the degree of truth for at least one out of both propositions.\"")));
c0_OR.addConstraint(equal(joinRef(join($this(), c0_security)), global(c0_Medium)));
c0_AND.extending(c0_Operator);
c0_AND.addConstraint(equal(joinRef(join($this(), c0_name)), constant("\"AND\"")));
c0_AND.addConstraint(equal(joinRef(join($this(), c0_description)), constant("\"The operator AND is applicable when opinions for two independent propositions are aggregated to produce a new opinion reflecting the degree of truth of both propositions simultaneously.\"")));
c0_AND.addConstraint(equal(joinRef(join($this(), c0_security)), global(c0_Medium)));
c0_NOT.extending(c0_Operator);
c0_NOT.addConstraint(equal(joinRef(join($this(), c0_name)), constant("\"NOT\"")));
c0_NOT.addConstraint(equal(joinRef(join($this(), c0_description)), constant("\"The operator NOT is applicable when an opinion about a proposition needs to be negated.\"")));
c0_NOT.addConstraint(equal(joinRef(join($this(), c0_security)), global(c0_Medium)));
c0_wFUSION.extending(c0_Operator);
c0_wFUSION.addConstraint(equal(joinRef(join($this(), c0_name)), constant("\"Weighted fusion\"")));
c0_wFUSION.addConstraint(equal(joinRef(join($this(), c0_description)), constant("\"The operator Weighted fusion, is used to aggregate several opinions about the same thing.\"")));
c0_wFUSION.addConstraint(equal(joinRef(join($this(), c0_security)), global(c0_Medium)));
c0_cFUSION.extending(c0_Operator);
c0_cFUSION.addConstraint(equal(joinRef(join($this(), c0_name)), constant("\"Conflict-aware fusion\"")));
c0_cFUSION.addConstraint(equal(joinRef(join($this(), c0_description)), constant("\"The operator Conflict-aware fusion, is additionally capable of dealing with the degree of conflict between opinions.\"")));
c0_cFUSION.addConstraint(equal(joinRef(join($this(), c0_security)), global(c0_Medium)));
c0_CertainTrust.extending(c0_Task);
c0_CertainTrust.addConstraint(equal(joinRef(join($this(), c1_description)), constant("\"CertainTrust\"")));
c0_op.refToUnique(c0_Operator);
c1_security.refToUnique(Int);
c0_CertainTrust.addConstraint(equal(joinRef(join($this(), c1_security)), joinRef(joinRef(join(joinRef(join($this(), c0_op)), c0_security)))));
c0_CertainTrust.addConstraint(implies(equal(joinRef(join($this(), c0_op)), global(c0_NONE)), equal(joinRef(join($this(), c1_security)), constant(3))));
c0_CertainTrust.addConstraint(implies(equal(joinRef(join($this(), c0_op)), global(c0_OR)), equal(joinRef(join($this(), c1_security)), constant(3))));
c0_CertainTrust.addConstraint(implies(equal(joinRef(join($this(), c0_op)), global(c0_AND)), equal(joinRef(join($this(), c1_security)), constant(3))));
c0_CertainTrust.addConstraint(implies(equal(joinRef(join($this(), c0_op)), global(c0_NOT)), equal(joinRef(join($this(), c1_security)), constant(3))));
c0_CertainTrust.addConstraint(implies(equal(joinRef(join($this(), c0_op)), global(c0_wFUSION)), equal(joinRef(join($this(), c1_security)), constant(3))));
c0_CertainTrust.addConstraint(implies(equal(joinRef(join($this(), c0_op)), global(c0_cFUSION)), equal(joinRef(join($this(), c1_security)), constant(3))));