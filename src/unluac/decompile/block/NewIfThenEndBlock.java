package unluac.decompile.block;

import java.util.ArrayList;
import java.util.List;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.Registers;
import unluac.decompile.branch.Branch;
import unluac.decompile.condition.AndCondition;
import unluac.decompile.condition.Condition;
import unluac.decompile.condition.OrCondition;
import unluac.decompile.condition.SetCondition;
import unluac.decompile.expression.Expression;
import unluac.decompile.operation.Operation;
import unluac.decompile.statement.Statement;
import unluac.parse.LFunction;

public class NewIfThenEndBlock extends Block {

  private final Condition cond;
  private final Registers r;
  private final List<Statement> statements;
  
  public NewIfThenEndBlock(LFunction function, Registers r, Condition cond, int begin, int end) {
    super(function, begin, end);
    this.r = r;
    this.cond = cond;
    this.statements = new ArrayList<Statement>(end - begin + 1);
  }
     
  @Override
  public void addStatement(Statement statement) {
    statements.add(statement);
  }
  
  @Override
  public boolean breakable() {
    return false;
  }
  
  @Override
  public boolean isContainer() {
    return true;
  }
  
  @Override
  public boolean isUnprotected() {
    return false;
  }
  
  @Override
  public int getLoopback() {
    throw new IllegalStateException();
  }
  
  @Override
  public Operation process(Decompiler d) {
    final int test = cond.register();
    if(statements.isEmpty() && test >= 0 && r.getUpdated(test, end - 1) >= begin) {
      Condition assign = new SetCondition(end - 1, test);
      Condition combined;
      
      if(cond.invertible()) {
        combined = new OrCondition(cond.inverse(), assign);
      } else {
        combined = new AndCondition(cond, assign);
      }
      final Condition fcombined = combined;
      return new Operation(end - 1) {
        
        @Override
        public Statement process(Registers r, Block block) {
          r.setValue(test, end - 1, fcombined.asExpression(r));
          return null;
        }
        
      };
    }
    return super.process(d);
  }
  
  @Override
  public void print(Decompiler d, Output out) {
    out.print("if ");
    cond.asExpression(r).print(d, out);
    out.print(" then");
    out.println();
    out.indent();
    Statement.printSequence(d, out, statements);
    out.dedent();
    out.print("end");
  }
    
}