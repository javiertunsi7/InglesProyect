import { useEffect, useState } from 'react';

/**
 * Tiny data-fetching hook that wraps the common load / error / data triad.
 * Pass an async function and the dependencies that should trigger a refetch.
 *
 * Example:
 *   const { data, loading, error } = useFetch(() => levelService.getByCategory(type), [type]);
 */
export function useFetch(fetcher, dependencies = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;
    setLoading(true);
    setError(null);
    fetcher()
      .then((result) => {
        if (isMounted) setData(result);
      })
      .catch((err) => {
        if (isMounted) setError(err.message);
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });
    return () => {
      isMounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencies);

  return { data, loading, error };
}
