// [Benjamin – #09, 2026-10-15] Création. Fabrique un faux JWT (non signé) pour les tests du front.
export function fakeJwt(expInSeconds: number): string {
  const encode = (o: object) => btoa(JSON.stringify(o)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_');
  const exp = Math.floor(Date.now() / 1000) + expInSeconds;
  return `${encode({ alg: 'HS256' })}.${encode({ sub: 'test@test.fr', exp })}.signature`;
}
