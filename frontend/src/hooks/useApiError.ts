/**
 * 统一错误处理 Hook。
 *
 * 所有 API mutation 的错误统一通过此 hook 展示给用户。
 * 避免在每个页面组件中重复编写错误处理逻辑。
 *
 * 使用方式：
 *   const { showError } = useApiError();
 *   mutate(..., { onError: showError });
 *
 * 设计要点：
 * - 只负责展示错误提示，不处理业务逻辑
 * - 错误信息优先使用 Error 实例的 message
 * - 兜底消息：操作失败，请重试
 */
import { message } from 'antd';

export const useApiError = () => {
  /** 将任意错误对象转换为友好的提示信息并展示 */
  const showError = (err: unknown) => {
    if (err instanceof Error) {
      message.error(err.message);
    } else {
      message.error('操作失败，请重试');
    }
  };
  return { showError };
};
