package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * V3-1트랜잭션 - 파라미터 연동, 풀을 고려한 종료 -> 트랜잭션 매니저
 * 문제점: jdbc 관련된 것을 그대로 가져다 사용 -> jdbc에서 jpa로 바뀔때 코드를 변경해야하는 문제 발생
 * 따라서, 바로 가져다 쓰는게 아닌 TransactionManager를 사용하여 서브시 계층을 순수하게 유지
 *
 * V3-2트랜잭션 - 트랜잭션 템플릿 -> jdbc 생성자 주입은 트랜잭션 매니저로 해결
 * -> but 트랜잭션이 시작하고 비즈니스 로직을 실행할 때에 commit, rollback 문장이 너무 자주 반복됨.
 * 따라서 transactionTemplate를 사용.
 *
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    // * 생성자에서 로직이 들어가야해서 롬복의 RequiredArgsConstructor 제거
    // * 의존관계로 트랜잭션 매니저를 주입받고 내부에서는 트랜잭션 템플릿을 사용한다.
    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        txTemplate.executeWithoutResult((status) -> {
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
        /*
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            //비즈니스 로직
            bizLogic(fromId, toId, money);
            transactionManager.commit(status);  //성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }*/
    }
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById( fromId);
        Member toMember = memberRepository.findById(toId);
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }
    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}